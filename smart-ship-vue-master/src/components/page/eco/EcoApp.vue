<template>
  <div class="eco-root" v-html="bodyHtml"></div>
</template>

<script>
import ecoBody from "./ecoBody.js";
import worldMap from "./worldMap.js";
import "./eco.css";
import "./eco-overrides.css";

export default {
  name: "EcoApp",
  data() {
    return { bodyHtml: ecoBody };
  },
  created() {
    // Inline handlers in the ported markup (e.g. the map <img onload>) may fire
    // before mounted() runs the real logic. Provide harmless stubs so they don't throw;
    // runEcoLogic() overwrites them with the real implementations.
    ["layoutMap", "zoomMap", "closeDrawer", "openDrawer", "acknowledgeAlert",
     "selectZone", "selectTrendZone", "updatePlayhead"].forEach((fn) => {
      if (typeof window[fn] === "undefined") window[fn] = function () {};
    });
  },
  mounted() {
    // let the ported tab bar drive the Vue route (one URL per tab)
    window.ecoSetRoute = (view) => {
      const cur = this.$route.params.view || "dashboard";
      if (cur !== view) this.$router.push("/eco/" + view).catch(() => {});
    };
    this.$nextTick(async () => {
      try {
        const mod = await import("./ecoLogic.js");
        mod.runEcoLogic();
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error("[eco] logic init failed", e);
      }
      this.enhanceTopbar();
      this.bindTabRouting();
      this.upgradeMap();
      // normalise /eco -> /eco/dashboard, then activate whichever tab the URL asked for
      const view = this.$route.params.view;
      if (!view) this.$router.replace("/eco/dashboard").catch(() => {});
      if (typeof window.ecoActivateView === "function") {
        window.ecoActivateView(view || "dashboard");
      }
    });
  },
  watch: {
    // back/forward or a pasted /eco/<tab> URL -> switch tab without re-routing
    "$route.params.view"(view) {
      if (typeof window.ecoActivateView === "function") {
        window.ecoActivateView(view || "dashboard");
      }
    },
  },
  beforeDestroy() {
    try { delete window.ecoSetRoute; } catch (e) { window.ecoSetRoute = undefined; }
  },
  methods: {
    // Delegate tab-bar clicks on the stable .eco-root element so they keep working
    // regardless of the ported markup, and give every tab its own URL.
    bindTabRouting() {
      if (this._tabRoutingBound) return;
      this._tabRoutingBound = true;
      this.$el.addEventListener("click", (e) => {
        const btn = e.target.closest && e.target.closest('nav.tabs button[data-view]');
        if (!btn) return;
        const view = btn.dataset.view;
        if (typeof window.ecoActivateView === "function") window.ecoActivateView(view);
        const cur = this.$route.params.view || "dashboard";
        if (cur !== view) this.$router.push("/eco/" + view).catch(() => {});
      });
    },
    // swap the low-res raster world map for a crisp vector one (same projection/size)
    upgradeMap() {
      const img = this.$el.querySelector("#mapImg");
      if (!img) return;
      img.addEventListener("load", () => {
        if (typeof window.layoutMap === "function") window.layoutMap();
      });
      img.src = worldMap;
    },
    enhanceTopbar() {
      const right = this.$el.querySelector(".topbar-right");
      if (!right || right.querySelector(".eco-avatarmenu")) return;

      const avatar = right.querySelector(".avatar");
      if (!avatar) return;

      // avatar initials from stored profile
      let name = "User";
      try {
        const info = JSON.parse(localStorage.getItem("userInfo") || "{}");
        name = info.nickName || info.userName || localStorage.getItem("username") || "User";
      } catch (e) { /* noop */ }
      avatar.textContent =
        name.replace(/[^A-Za-z ]/g, "").trim().split(/\s+/).map((s) => s[0]).join("").slice(0, 2).toUpperCase() || "U";

      // wrap the avatar so we can hang a dropdown off it
      const wrap = document.createElement("div");
      wrap.className = "eco-avatarmenu";
      avatar.parentNode.insertBefore(wrap, avatar);
      wrap.appendChild(avatar);
      avatar.style.cursor = "pointer";

      const pop = document.createElement("div");
      pop.className = "eco-usermenu-pop";
      pop.innerHTML = '<a href="javascript:void(0)" class="eco-logout">Sign out</a>';
      wrap.appendChild(pop);

      avatar.addEventListener("click", (e) => {
        e.stopPropagation();
        wrap.classList.toggle("open");
      });
      document.addEventListener("click", () => wrap.classList.remove("open"));
      pop.querySelector(".eco-logout").addEventListener("click", () => {
        try {
          localStorage.removeItem("t");
          localStorage.removeItem("menu");
          localStorage.removeItem("menu1");
        } catch (e) { /* noop */ }
        this.$router.push("/login");
      });
    },
  },
};
</script>

<style>
/* avatar dropdown injected by EcoApp.enhanceTopbar */
.eco-avatarmenu { position: relative; }
.eco-usermenu-pop {
  position: absolute; right: 0; top: calc(100% + 10px);
  background: #fff; border: 1px solid var(--border, #E1E7F2);
  border-radius: 10px; box-shadow: 0 8px 28px rgba(8,19,44,0.22);
  min-width: 160px; padding: 6px; display: none; z-index: 200;
}
.eco-avatarmenu.open .eco-usermenu-pop { display: block; }
.eco-usermenu-pop a {
  display: block; padding: 9px 10px; font-size: 13px;
  border-radius: 6px; text-decoration: none;
}
.eco-usermenu-pop a:hover { background: #F2F5FB; }
.eco-usermenu-pop a.eco-logout { color: #E5484D; font-weight: 600; }
</style>
