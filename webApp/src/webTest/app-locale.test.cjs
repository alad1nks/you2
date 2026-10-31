const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const { join } = require("node:path");
const { runInNewContext } = require("node:vm");
const { test } = require("node:test");

test("language changes notify Compose and System restores live browser languages", () => {
    let systemLanguages = ["ru-RU", "en-US"];
    class Navigator {
        get languages() { return systemLanguages; }
    }
    const events = [];
    const context = {
        Navigator,
        navigator: new Navigator(),
        document: { documentElement: {} },
        window: { dispatchEvent: (event) => events.push(event.type) },
        Event: class { constructor(type) { this.type = type; } }
    };
    runInNewContext(readFileSync(join(__dirname, "../webMain/resources/app-locale.js"), "utf8"), context);
    assert.equal(context.document.documentElement.lang, "ru-RU");
    for (const language of ["en", "ru", "kk"]) {
        context.window.setYou2Locale(language);
        assert.deepEqual(Array.from(context.navigator.languages), [language]);
        assert.equal(context.document.documentElement.lang, language);
    }
    context.window.setYou2Locale("kk");
    assert.equal(events.length, 3);
    systemLanguages = ["en-GB"];
    context.window.setYou2Locale(null);
    assert.deepEqual(context.navigator.languages, ["en-GB"]);
    assert.equal(context.document.documentElement.lang, "en-GB");
    assert.deepEqual(events, Array(4).fill("languagechange"));
});
