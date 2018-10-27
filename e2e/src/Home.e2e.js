describe("Home", function () {
    it("should show the home page", function () {
        browser.get("/");

        expect(browser.getTitle()).toBe("IIoT");
    });

    it("should show the current status of the valve", async function () {
        browser.get("/");

        const valveStatus = element(by.css('.valve-status'));
        await browser.wait(async () => valveStatus.isPresent()
            , 10000);

        expect(valveStatus.isPresent()).toBe(true);
    });
});