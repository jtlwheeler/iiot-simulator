describe("Home", function () {
    it("should show the home page", function () {
        browser.get("/");

        expect(browser.getTitle()).toBe("IIoT");
    });
});