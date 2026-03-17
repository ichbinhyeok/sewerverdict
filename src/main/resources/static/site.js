const EVENT_URL = "/api/events";
const ATTRIBUTION_STORAGE_KEY = "sv-attribution";
const ATTRIBUTION_PARAM_ALIASES = {
	utmSource: ["utmSource", "utm_source"],
	utmMedium: ["utmMedium", "utm_medium"],
	utmCampaign: ["utmCampaign", "utm_campaign"],
	utmTerm: ["utmTerm", "utm_term"],
	utmContent: ["utmContent", "utm_content"],
	gclid: ["gclid"],
	wbraid: ["wbraid"],
	gbraid: ["gbraid"]
};
const ATTRIBUTION_KEYS = Object.keys(ATTRIBUTION_PARAM_ALIASES);

function postEvent(eventType, pageSlug, label, metadata = {}) {
	if (!eventType || !pageSlug) {
		return;
	}
	const payload = JSON.stringify({ eventType, pageSlug, label, metadata });
	if (navigator.sendBeacon) {
		navigator.sendBeacon(EVENT_URL, new Blob([payload], { type: "application/json" }));
		return;
	}
	fetch(EVENT_URL, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: payload,
		keepalive: true
	}).catch(() => {});
}

function readUrlAttribution() {
	const params = new URLSearchParams(window.location.search);
	const attribution = {};
	ATTRIBUTION_KEYS.forEach((key) => {
		const value = ATTRIBUTION_PARAM_ALIASES[key]
			.map((paramName) => params.get(paramName))
			.find((candidate) => candidate);
		if (value) {
			attribution[key] = value;
		}
	});
	return attribution;
}

function readStoredAttribution() {
	try {
		const raw = localStorage.getItem(ATTRIBUTION_STORAGE_KEY);
		if (!raw) {
			return {};
		}
		return JSON.parse(raw);
	}
	catch {
		return {};
	}
}

function persistAttribution() {
	const stored = readStoredAttribution();
	const current = readUrlAttribution();
	const merged = { ...stored, ...current };
	if (Object.keys(merged).length) {
		try {
			localStorage.setItem(ATTRIBUTION_STORAGE_KEY, JSON.stringify(merged));
		}
		catch {
			return merged;
		}
	}
	return merged;
}

function applyAttributionToLinks(attribution) {
	if (!Object.keys(attribution).length) {
		return;
	}
	document.querySelectorAll("a[href^='/']").forEach((link) => {
		if (link.dataset.noAttribution === "true") {
			return;
		}
		const url = new URL(link.getAttribute("href"), window.location.origin);
		ATTRIBUTION_KEYS.forEach((key) => {
			const hasAttributionParam = ATTRIBUTION_PARAM_ALIASES[key]
				.some((paramName) => url.searchParams.has(paramName));
			if (attribution[key] && !hasAttributionParam) {
				url.searchParams.set(key, attribution[key]);
			}
		});
		link.setAttribute("href", `${url.pathname}${url.search}${url.hash}`);
	});
}

function applyAttributionToForms(attribution) {
	document.querySelectorAll("form[data-preserve-attribution]").forEach((form) => {
		ATTRIBUTION_KEYS.forEach((key) => {
			const field = form.querySelector(`[name='${key}']`);
			if (field && attribution[key] && !field.value) {
				field.value = attribution[key];
			}
		});
	});
}

document.addEventListener("click", (event) => {
	const target = event.target.closest("[data-event-type]");
	if (!target) {
		return;
	}
	postEvent(target.dataset.eventType, target.dataset.pageSlug, target.dataset.eventLabel || target.textContent.trim(), {
		draftId: target.dataset.eventDraftId || "",
		route: target.dataset.eventRoute || "",
		step: target.dataset.eventStep || "",
		city: target.dataset.eventCity || "",
		geoPage: target.dataset.eventGeoPage || "",
		geoFamily: target.dataset.eventGeoFamily || "",
		placement: target.dataset.eventPlacement || ""
	});
});

document.addEventListener("DOMContentLoaded", () => {
	const attribution = persistAttribution();
	applyAttributionToLinks(attribution);
	applyAttributionToForms(attribution);
	const revealAndFocus = (element) => {
		if (!element) {
			return;
		}
		element.scrollIntoView({ behavior: "smooth", block: "center" });
		window.setTimeout(() => {
			try {
				element.focus({ preventScroll: true });
			}
			catch {
				element.focus();
			}
		}, 120);
	};

	const markStepAttention = (step) => {
		if (!step) {
			return;
		}
		step.classList.add("step-needs-attention");
		window.setTimeout(() => step.classList.remove("step-needs-attention"), 1400);
	};

	const navToggle = document.querySelector("[data-nav-toggle]");
	const navMenu = document.querySelector("[data-nav-menu]");
	if (navToggle && navMenu) {
		const syncNav = (open) => {
			navToggle.setAttribute("aria-expanded", String(open));
			navMenu.dataset.open = open ? "true" : "false";
		};
		syncNav(false);
		navToggle.addEventListener("click", () => {
			syncNav(navToggle.getAttribute("aria-expanded") !== "true");
		});
		navMenu.querySelectorAll("a").forEach((link) => {
			link.addEventListener("click", () => syncNav(false));
		});
	}

	const estimatorShell = document.querySelector("[data-estimator]");
	if (estimatorShell) {
		postEvent("estimator_start", "/estimator/", "estimator-view");

		const steps = Array.from(estimatorShell.querySelectorAll("[data-step]"));
		const nextButton = estimatorShell.querySelector("[data-step-next]");
		const backButton = estimatorShell.querySelector("[data-step-back]");
		const submitButton = estimatorShell.querySelector("[data-step-submit]");
		const progressFill = estimatorShell.querySelector("[data-progress-fill]");
		const progressLabel = estimatorShell.querySelector("[data-progress-label]");
		const stepError = estimatorShell.querySelector("[data-step-error]");
		const wizardForm = estimatorShell.querySelector(".wizard-form");
		let activeIndex = 0;
		let showValidation = false;
		let lastTrackedStep = -1;

		const currentStep = () => steps[activeIndex];
		const firstInvalidControl = (step) => {
			if (!step) {
				return null;
			}
			const radioInputs = Array.from(step.querySelectorAll("input[type='radio']"));
			if (radioInputs.length && !radioInputs.some((input) => input.checked)) {
				return radioInputs[0];
			}
			return Array.from(step.querySelectorAll("input[type='text'], input[type='email'], input[type='tel'], textarea, select"))
				.find((field) => !(field.value && field.value.trim().length > 0)) || null;
		};
		const revealCurrentStepIssue = () => {
			const step = currentStep();
			markStepAttention(step);
			revealAndFocus(firstInvalidControl(step) || stepError || step);
		};

		const stepIsValid = (step) => {
			if (!step) {
				return false;
			}
			const radioInputs = Array.from(step.querySelectorAll("input[type='radio']"));
			if (radioInputs.length) {
				return radioInputs.some((input) => input.checked);
			}
			const fields = Array.from(step.querySelectorAll("input[type='text'], input[type='email'], input[type='tel'], textarea, select"));
			if (!fields.length) {
				return true;
			}
			return fields.every((field) => field.value && field.value.trim().length > 0);
		};

		const validationMessage = () => currentStep()?.dataset.stepMessage || "Please complete this step before continuing.";

		const updateStepControls = () => {
			const valid = stepIsValid(currentStep());
			if (nextButton) {
				nextButton.classList.toggle("is-disabled", !valid);
			}
			if (submitButton) {
				submitButton.classList.toggle("is-disabled", !valid);
			}
			if (stepError) {
				stepError.textContent = validationMessage();
				stepError.hidden = valid || !showValidation;
			}
			currentStep()?.classList.toggle("step-needs-attention", !valid && showValidation);
		};

		const findFirstIncompleteStepIndex = () => {
			const idx = steps.findIndex((step) => !stepIsValid(step));
			return idx === -1 ? 0 : idx;
		};

		const syncWizard = () => {
			steps.forEach((step, index) => {
				step.hidden = index !== activeIndex;
			});
			backButton.hidden = activeIndex === 0;
			nextButton.hidden = activeIndex === steps.length - 1;
			submitButton.hidden = activeIndex !== steps.length - 1;
			progressFill.style.width = `${((activeIndex + 1) / steps.length) * 100}%`;
			progressLabel.textContent = `Step ${activeIndex + 1} of ${steps.length}`;
			if (lastTrackedStep !== activeIndex) {
				postEvent("estimator_step_view", "/estimator/", `step-${activeIndex + 1}`, {
					step: String(activeIndex + 1)
				});
				lastTrackedStep = activeIndex;
			}
			updateStepControls();
		};

		nextButton?.addEventListener("click", () => {
			if (stepIsValid(currentStep()) && activeIndex < steps.length - 1) {
				showValidation = false;
				activeIndex += 1;
				syncWizard();
				return;
			}
			showValidation = true;
			postEvent("estimator_step_validation_error", "/estimator/", `step-${activeIndex + 1}`, {
				step: String(activeIndex + 1)
			});
			updateStepControls();
			revealCurrentStepIssue();
		});

		backButton?.addEventListener("click", () => {
			if (activeIndex > 0) {
				showValidation = false;
				activeIndex -= 1;
				syncWizard();
			}
		});

		wizardForm?.addEventListener("submit", (event) => {
			if (!stepIsValid(currentStep())) {
				event.preventDefault();
				showValidation = true;
				postEvent("estimator_step_validation_error", "/estimator/", `step-${activeIndex + 1}`, {
					step: String(activeIndex + 1)
				});
				updateStepControls();
				revealCurrentStepIssue();
			}
		});

		wizardForm?.addEventListener("input", updateStepControls);
		wizardForm?.addEventListener("change", updateStepControls);
		activeIndex = findFirstIncompleteStepIndex();
		syncWizard();
	}

	document.querySelectorAll("form[data-lead-form]").forEach((form) => {
		const formError = form.parentElement?.querySelector("[data-form-error]");
		const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		const fieldConfigs = {
			serviceNeeded: {
				validate: (field) => field.value ? "" : "Choose the type of help you need first."
			},
			zipOrCity: {
				validate: (field) => field.value.trim() ? "" : "Enter the ZIP code or city for this property."
			},
			name: {
				validate: (field) => field.value.trim() ? "" : "Enter your name so the reply can be addressed correctly."
			},
			email: {
				validate: (field) => {
					const value = field.value.trim();
					if (!value) {
						return "Enter an email address for follow-up.";
					}
					return emailPattern.test(value) ? "" : "Enter a valid email address like name@example.com.";
				}
			},
			phone: {
				validate: (field) => field.value.trim() ? "" : "Enter the best phone number for a reply."
			},
			consentGiven: {
				validate: (field) => field.checked ? "" : "Consent is required before the form can be submitted."
			}
		};

		const errorNodeFor = (name) => form.querySelector(`[data-error-for='${name}']`);
		const controlFor = (name) => form.querySelector(`[name='${name}']`);
		const wrapperFor = (field) => field?.closest(".field-label, .consent-row");
		const setFormError = (message) => {
			if (!formError) {
				return;
			}
			if (message) {
				formError.textContent = message;
				formError.hidden = false;
				return;
			}
			formError.hidden = true;
		};
		const messageForField = (name) => {
			const field = controlFor(name);
			const config = fieldConfigs[name];
			if (!field || !config) {
				return "";
			}
			return config.validate(field);
		};

		const validateField = (name) => {
			const field = controlFor(name);
			if (!field) {
				return "";
			}
			const message = messageForField(name);
			const wrapper = wrapperFor(field);
			const errorNode = errorNodeFor(name);
			field.setAttribute("aria-invalid", message ? "true" : "false");
			wrapper?.classList.toggle("has-error", Boolean(message));
			if (errorNode) {
				errorNode.textContent = message;
			}
			return message;
		};

		const invalidFields = (sync = false) => Object.keys(fieldConfigs)
			.map((name) => ({ name, message: sync ? validateField(name) : messageForField(name) }))
			.filter((entry) => entry.message);

		const clearFieldOnInput = (field) => {
			if (!field) {
				return;
			}
			const name = field.getAttribute("name");
			if (!name || !fieldConfigs[name]) {
				return;
			}
			validateField(name);
			if (!invalidFields(false).length) {
				setFormError("");
			}
		};

		form.addEventListener("submit", (event) => {
			const invalid = invalidFields(true);
			if (!invalid.length) {
				setFormError("");
				return;
			}
			event.preventDefault();
			setFormError("Please fix the highlighted fields before submitting.");
			const firstField = controlFor(invalid[0].name);
			revealAndFocus(firstField || formError || form);
		});

		Object.keys(fieldConfigs).forEach((name) => {
			const field = controlFor(name);
			field?.addEventListener("input", () => clearFieldOnInput(field));
			field?.addEventListener("change", () => clearFieldOnInput(field));
			field?.addEventListener("blur", () => clearFieldOnInput(field));
		});
	});

	const copyButtons = Array.from(document.querySelectorAll("[data-copy-summary]"));
	if (copyButtons.length) {
		copyButtons.forEach((copyButton) => copyButton.addEventListener("click", async () => {
			const target = document.querySelector("#summary-block");
			if (!target) {
				return;
			}
			try {
				await navigator.clipboard.writeText(target.textContent.trim());
				copyButtons.forEach((button) => {
					button.textContent = "Summary copied";
				});
			}
			catch {
				copyButtons.forEach((button) => {
					button.textContent = "Copy manually below";
				});
			}
		}));
	}
});
