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
	const estimatorShell = document.querySelector("[data-estimator]");
	if (estimatorShell) {
		postEvent("estimator_start", "/estimator/", "estimator-view");

		const steps = Array.from(estimatorShell.querySelectorAll("[data-step]"));
		const nextButton = estimatorShell.querySelector("[data-step-next]");
		const backButton = estimatorShell.querySelector("[data-step-back]");
		const submitButton = estimatorShell.querySelector("[data-step-submit]");
		const progressFill = estimatorShell.querySelector("[data-progress-fill]");
		const progressLabel = estimatorShell.querySelector("[data-progress-label]");
		let activeIndex = 0;

		const syncWizard = () => {
			steps.forEach((step, index) => {
				step.hidden = index !== activeIndex;
			});
			backButton.hidden = activeIndex === 0;
			nextButton.hidden = activeIndex === steps.length - 1;
			submitButton.hidden = activeIndex !== steps.length - 1;
			progressFill.style.width = `${((activeIndex + 1) / steps.length) * 100}%`;
			progressLabel.textContent = `Step ${activeIndex + 1} of ${steps.length}`;
		};

		nextButton?.addEventListener("click", () => {
			if (activeIndex < steps.length - 1) {
				activeIndex += 1;
				syncWizard();
			}
		});

		backButton?.addEventListener("click", () => {
			if (activeIndex > 0) {
				activeIndex -= 1;
				syncWizard();
			}
		});

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
