import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import vm from "node:vm";

class FakeElement {
  constructor(tag = "div") {
    this.tag = tag;
    this.children = [];
    this.handlers = {};
    this.classesRemoved = [];
    this.classList = { remove: (name) => this.classesRemoved.push(name) };
    this.disabled = false;
  }
  replaceChildren(...nodes) { this.children = nodes; }
  append(...nodes) { this.children.push(...nodes); }
  setAttribute(name, value) { this[name] = value; }
  addEventListener(name, handler) { this.handlers[name] = handler; }
}
const sandbox = { window: {}, document: { createElement: tag => new FakeElement(tag) }, console };
vm.runInNewContext(readFileSync("src/main/resources/static/rescue-ui.js", "utf8"), sandbox);
const rescue = sandbox.window.FornitoriRescue;

test("network and server errors are recoverable, not validation or auth", () => {
  assert.equal(rescue.isTransient(new Error("offline")), true);
  assert.equal(rescue.isTransient({ status: 503 }), true);
  for (const status of [400, 401, 403, 404, 409, 429]) assert.equal(rescue.isTransient({ status }), false);
});
test("server text is never parsed as HTML", () => {
  const target = new FakeElement();
  rescue.render(target, "<img onerror=alert(1)>", null);
  assert.equal(target.children[0].children[1].children[1].textContent, "<img onerror=alert(1)>");
  assert.deepEqual(target.classesRemoved, ["d-none"]);
});
test("retry requires a click and prevents duplicate clicks", async () => {
  let count = 0, resolve;
  const target = new FakeElement();
  rescue.render(target, "error", () => { count++; return new Promise(r => { resolve = r; }); });
  const button = target.children[0].children[1].children[2];
  assert.equal(count, 0);
  const pending = button.handlers.click();
  assert.equal(button.disabled, true);
  await button.handlers.click();
  assert.equal(count, 1);
  resolve();
  await pending;
  assert.equal(button.disabled, false);
});
