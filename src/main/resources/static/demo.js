(() => {
  "use strict";
  const status = document.getElementById("demoStatus");
  const buttons = [...document.querySelectorAll("[data-profile]")];
  async function enter(profile) {
    buttons.forEach(button => { button.disabled = true; });
    status.textContent = "Preparazione dell’accesso… Il primo avvio può richiedere qualche secondo.";
    try {
      const csrfResponse = await fetch("/api/v1/auth/csrf", { credentials: "same-origin", cache: "no-store" });
      if (!csrfResponse.ok) {
        const error = new Error("Il servizio si sta avviando. Riprova tra qualche secondo.");
        error.status = csrfResponse.status;
        throw error;
      }
      const csrf = await csrfResponse.json();
      const response = await fetch("/api/v1/demo/login", {
        method: "POST", credentials: "same-origin", cache: "no-store",
        headers: { "Content-Type": "application/json", [csrf.headerName]: csrf.token },
        body: JSON.stringify({ profilo: profile })
      });
      if (!response.ok) {
        const error = new Error(response.status === 429 ? "Attendi un minuto prima di riprovare." : "Accesso non riuscito. Riprova tra qualche secondo.");
        error.status = response.status;
        throw error;
      }
      location.assign("/");
    } catch (error) {
      buttons.forEach(button => { button.disabled = false; });
      if (window.FornitoriRescue?.isTransient(error)) {
        window.FornitoriRescue.render(status, error.message, () => enter(profile));
      } else {
        status.textContent = error.message;
      }
    }
  }
  buttons.forEach(button => button.addEventListener("click", () => enter(button.dataset.profile)));
  const profile = new URLSearchParams(location.search).get("profilo");
  if (["centrale", "autista"].includes(profile)) void enter(profile);
})();
