const HtmlScreenshotReporter = require('protractor-jasmine2-screenshot-reporter');
const jasmineReporters = require('jasmine-reporters');

const screenshotReporter = new HtmlScreenshotReporter({
    dest: 'screenshots',
    filename: 'report.html'
});

let jUnitXmlReporter = new jasmineReporters.JUnitXmlReporter({
    consolidateAll: true,
    filePrefix: 'e2e',
    savePath: 'build/test-results/e2e'
});

exports.config = {
    specs: ['src/**/*.e2e.js'],
    capabilities: {
        browserName: process.env.BROWSER || 'chrome',
        seleniumAddress: process.env.SELENIUM_ADDRESS
    },
    baseUrl: process.env.PROTRACTOR_BASE_URL || 'http://localhost:3001',
    onPrepare: async function () {
        console.log(`Base URL: ${browser.baseUrl}`);
        jasmine.getEnv().addReporter(screenshotReporter);
        jasmine.getEnv().addReporter(jUnitXmlReporter);
        browser.ignoreSynchronization = true;
        await browser.wait(async () => {
            await browser.get(browser.baseUrl);
        return await element(By.css('#root')).isPresent();
    }, 60 * 1000)
    },
    beforeLaunch: function () {
        return new Promise(function (resolve) {
            screenshotReporter.beforeLaunch(resolve);
        });
    },

    afterLaunch: function (exitCode) {
        return new Promise(function (resolve) {
            screenshotReporter.afterLaunch(resolve.bind(this, exitCode));
        });
    }
};