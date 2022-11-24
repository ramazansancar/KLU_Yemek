/**
 *	Bu betik, uygulamada kullanlılan 'list.json'
 *	dosyasını otomatik olarak güncellemeye yarar.
 *	Node.js ile çalışır (node index.js).
 *			Berkant Korkmaz - berkantkz
 */

var jsdom = require("jsdom");
const { JSDOM } = jsdom;
const { window } = new JSDOM();
const { document } = (new JSDOM('')).window;
global.document = document;

var $ = jQuery = require('jquery')(window);
const fs = require('fs');
const axios = require('axios');
const path = require('path');

var website = "";

String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
};

fetchData = async () => {
    await axios.get(`https://sks.klu.edu.tr/Takvimler/73-yemek-takvimi.klu`,{
        headers: {
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36',
            'Referer': 'https://sks.klu.edu.tr/Takvimler/73-yemek-takvimi.klu',
        }
        }).then( ({
            status,
            data,
            headers
        }) => {
            if(status == 200){
                website = data;
                var jsonIn = $(website).find("textarea")[0].innerHTML;
                var getDay = $.parseJSON(jsonIn);
                var currentMonth = getDay[0].start.replace(" 00:00:00 ", "").split('-')[1];
                var counter = 0;
                var out = [];

                do {
                    //console.log(getDay[counter]);
                    out.push({
                        day: getDay[counter].title,
                        date: getDay[counter].start.replace("00:00:00", "").trim(),
                        content: getDay[counter].aciklama.trim()
                    });
                    console.log(out[counter])
                    counter++;
                }while(
                    getDay[counter].start.includes(currentMonth)
                );

                var out_all = [];
                var counter_all = 0;
                getDay.forEach(item => {
                    out_all.push({
                        day: getDay[counter_all].title,
                        date: getDay[counter_all].start.replace("00:00:00", "").trim(),
                        content: getDay[counter_all].aciklama.trim()
                    });
                    //console.log(out[counter_all])
                    counter_all++;
                });

                fs.writeFile("list.json", JSON.stringify(out, null, 2), function(error) {
                    (error ? console.log(error) : console.log("*** \nlist.json was written.\n***"));
                });

                fs.writeFile("list_all.json", JSON.stringify(out_all, null, 2), function(error) {
                    (error ? console.log(error) : console.log("*** \nlist_all.json was written.\n***"));
                });

                fs.writeFile("list_raw.json", JSON.stringify(getDay, null, 2), function(error) {
                    (error ? console.log(error) : console.log("*** \nlist_raw.json was written.\n***"));
                });

            }else{
                console.log(status)
                console.log(data)
            }
        }).catch((e) => {
            console.log(e);
        });
};
fetchData();