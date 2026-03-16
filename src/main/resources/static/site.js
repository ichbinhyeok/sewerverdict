const EVENT_URL = "/api/events";

function postEvent(eventType, pageSlug, label) {
	if (!eventType || !pageSlug) {
		return;
	}
	const payload = JSON.stringify({ eventType, pageSlug, label });
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

document.addEventListener("click", (event) => {
	const target = event.target.closest("[data-event-type]");
	if (!target) {
		return;
	}
	postEvent(target.dataset.eventType, target.dataset.pageSlug, target.dataset.eventLabel || target.textContent.trim());
});

document.addEventListener("DOMContentLoaded", () => {
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

		const currentStep = () => steps[activeIndex];

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

		const updateStepControls = () => {
			const valid = stepIsValid(currentStep());
			if (nextButton) {
				nextButton.classList.toggle("is-disabled", !valid);
			}
			if (submitButton) {
				submitButton.classList.toggle("is-disabled", !valid);
			}
			if (stepError) {
				stepError.hidden = valid || !showValidation;
			}
		};

		const findFirstIncompleteStepIndex = () => {
			const idx = steps.findIndex((step) => !stepIsValid(step));
			return idx === -1 ? 0 : idx;
		};

		const applyPrefillFromUrl = () => {
			const params = new URLSearchParams(window.location.search);
			const role = params.get("role");
			const urgency = params.get("urgency");
			if (role) {
				const target = wizardForm?.querySelector(`input[name='role'][value='${role}']`);
				if (target) {
					target.checked = true;
				}
			}
			if (urgency) {
				const target = wizardForm?.querySelector(`input[name='urgency'][value='${urgency}']`);
				if (target) {
					target.checked = true;
				}
			}
			activeIndex = findFirstIncompleteStepIndex();
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
			updateStepControls();
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
				updateStepControls();
			}
		});

		wizardForm?.addEventListener("input", updateStepControls);
		wizardForm?.addEventListener("change", updateStepControls);
		applyPrefillFromUrl();
		syncWizard();
	}

	const copyButton = document.querySelector("[data-copy-summary]");
	if (copyButton) {
		copyButton.addEventListener("click", async () => {
			const target = document.querySelector("#summary-block");
			if (!target) {
				return;
			}
			try {
				await navigator.clipboard.writeText(target.textContent.trim());
				copyButton.textContent = "Summary copied";
			}
			catch (error) {
				copyButton.textContent = "Copy manually below";
			}
		});
	}
});
