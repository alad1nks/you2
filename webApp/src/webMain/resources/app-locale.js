// Compose reads navigator.languages. Keep the browser getter for the System option.
(() => {
    const systemLanguages = Object.getOwnPropertyDescriptor(Navigator.prototype, "languages");
    let appLocale = null;

    Object.defineProperty(Navigator.prototype, "languages", {
        ...systemLanguages,
        get() {
            return appLocale ? [appLocale] : systemLanguages.get.call(this);
        }
    });

    window.setYou2Locale = (value) => {
        if (appLocale === value) return;
        appLocale = value;
        document.documentElement.lang = value || navigator.languages[0] || "en";
        // Compose caches browser locales until this event is received.
        window.dispatchEvent(new Event("languagechange"));
    };
    document.documentElement.lang = navigator.languages[0] || "en";
})();
