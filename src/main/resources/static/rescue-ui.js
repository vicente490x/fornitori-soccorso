(() => {
  "use strict";
  function isTransient(error) {
    if (!error) return false;
    const status = error.status;
    return status == null || status === 0 || status === 408 || (Number.isInteger(status) && status >= 500);
  }
  function render(target, message, retry) {
    if (!target) return;
    target.replaceChildren();
    const panel = document.createElement("div");
    panel.className = "rescue-panel";
    const mascot = document.createElement("img");
    mascot.src = "/images/rescue-cat.svg";
    mascot.alt = "";
    mascot.className = "rescue-panel__mascot";
    mascot.setAttribute("aria-hidden", "true");
    const content = document.createElement("div");
    content.className = "rescue-panel__content";
    const heading = document.createElement("strong");
    heading.className = "rescue-panel__title";
    heading.textContent = "Miao! Un imprevisto sulla strada.";
    const body = document.createElement("p");
    body.className = "rescue-panel__message";
    body.textContent = typeof message === "string" && message.trim() ? message : "Servizio temporaneamente non disponibile.";
    content.append(heading, body);
    if (typeof retry === "function") {
      const button = document.createElement("button");
      button.type = "button";
      button.className = "rescue-panel__retry";
      button.textContent = "Riprova il caricamento";
      button.addEventListener("click", async () => {
        if (button.disabled) return;
        button.disabled = true;
        try { await retry(); }
        catch (error) { console.error("Riprova non riuscita:", error); }
        finally { button.disabled = false; }
      });
      content.append(button);
    }
    panel.append(mascot, content);
    target.append(panel);
    target.classList.remove("d-none");
  }
  window.FornitoriRescue = Object.freeze({ isTransient, render });
})();
