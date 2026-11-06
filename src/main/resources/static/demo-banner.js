(() => {
  "use strict";
  fetch("/api/v1/demo/info", { credentials: "same-origin", cache: "no-store" })
    .then(response => response.ok ? response.json() : null)
    .then(info => {
      if (!info?.demo) return;
      document.documentElement.classList.add("is-demo");
      const banner = document.createElement("aside");
      banner.className = "demo-bar";
      const label = document.createElement("span");
      label.textContent = "DEMO · Dati inventati · Nessun soccorso reale";
      const link = document.createElement("a");
      link.href = "/demo.html";
      link.textContent = "Cambia profilo ↗";
      banner.append(label, link);
      document.body.prepend(banner);
      document.querySelectorAll('a[href^="https://t.me/"]').forEach(anchor => {
        anchor.href = "/demo.html";
        anchor.removeAttribute("target");
        anchor.setAttribute("aria-label", "Esplora la demo");
        if (anchor.classList.contains("contact-button")) anchor.textContent = "Esplora la demo →";
      });
    }).catch(() => {});
})();
