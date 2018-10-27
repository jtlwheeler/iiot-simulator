describe("Home", function () {
    it("should show the home page", function () {
        browser.get("/");

        expect(browser.getTitle()).toBe("IIoT");
    });

    it("should show the current status of the valve", function () {
        browser.get("/");

        const valveStatus = element(by.css('.valve-status'));
        expect(valveStatus.getText()).toBe('0');
    });
});