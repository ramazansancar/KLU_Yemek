/**
 *	Bu betik, uygulamada kullanlılan 'list.json'
 *	dosyasını otomatik olarak güncellemeye yarar.
 *	Node.js ile çalışır (node index.js).
 *			Berkant Korkmaz - berkantkz
 */

var jsdom = require("jsdom");
const {
    JSDOM
} = jsdom;
const {
    window
} = new JSDOM();
const {
    document
} = (new JSDOM('')).window;
global.document = document;

var $ = jQuery = require('jquery')(window);
const fs = require('fs');
const curl = new(require('curl-request'))();
const path = require('path');

var website = "";

    curl.setHeaders([
        'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36'
    ])
    .get('https://sks.klu.edu.tr/Takvimler/73-yemek-takvimi.klu')
    .then(({
        statusCode,
        body,
        headers
    }) => {
        website = body;
        var jsonIn = $(website).find("textarea")[0].innerHTML;

        var getDay = $.parseJSON(jsonIn);
        var currentMonth = getDay[0].start.replace(" 00:00:00 ", "").split('-')[1];
        var counter = 0;
        var out = [];

        do {
            console.log(getDay[counter]);
            out.push({
                day: getDay[counter].title,
                date: getDay[counter].start.replace("00:00:00", "").trim(),
                content: getDay[counter].aciklama.trim()
            });
            counter++;
        } while (
            getDay[counter].start.includes(currentMonth)
        );

        fs.writeFile("../list.json", JSON.stringify(out, null, 2), function(error) {
            (error ? console.log(error) : console.log("*** \nlist.json was written.\n***"));
        });
    })
    .catch((e) => {
        console.log(e);
    });
