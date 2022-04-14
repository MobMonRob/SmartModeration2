const header = document.getElementById("header");
var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var consensusLevelList;
var voiceList;
var colors = [];
var countJSON;
var totalVoices = 6;
var labels = [];
var voicesMap = new Map();
var chartValues = [];
var notVoted;
var voted = 0;
var countRow = 0;
var count = 0;
const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const pollId = urlParams.get('pollId');
const table = document.getElementById("legendTable");
const h2 = document.getElementById("h2");
const detailTable = document.getElementById("detailTable");

function loadVoiceJSON() {

    var xobj = new XMLHttpRequest();
    xobj.open('GET', '/result/voices?pollId=' + urlParams.get('pollId'), true);

    xobj.onreadystatechange = function () {

        if (xobj.readyState == 4 && xobj.status == '200') {
            voiceList = JSON.parse(xobj.responseText);
            loadConsensusLevelsJSON();
        }
    };

    xobj.send(null);
}

function loadConsensusLevelsJSON() {

    var xobj = new XMLHttpRequest();
    xobj.open('GET', '/result/consensusLevels', true);

    xobj.onreadystatechange = function () {

        if (xobj.readyState == 4 && xobj.status == '200') {
           consensusLevelList = JSON.parse(xobj.responseText);
           loadMembersJSON();

        }
    };

    xobj.send(null);
}

function loadMembersJSON() {

    var xobj = new XMLHttpRequest();
    xobj.open('GET', '/result/members', true);

    xobj.onreadystatechange = function () {

        if (xobj.readyState == 4 && xobj.status == '200') {

           countJSON = JSON.parse(xobj.responseText);
           totalVoices = countJSON.count;

            header.innerHTML = "Ergebnis der Abstimmung: " + urlParams.get('name');

            for (var i = 0; i < consensusLevelList.consensusLevels.length; i++) {

                var count = 0;

                colors.push(consensusLevelList['consensusLevels'][i].color);
                voicesMap.set(consensusLevelList['consensusLevels'][i].name, count);
            }

            for (var i = 0; i < voiceList.voices.length; i++) {

                var clId = voiceList['voices'][i].consensusLevel;

                for (var j = 0; j < consensusLevelList.consensusLevels.length; j++) {

                    if (consensusLevelList['consensusLevels'][j].id === clId) {
                        voicesMap.set(consensusLevelList['consensusLevels'][j].name, voicesMap.get(consensusLevelList['consensusLevels'][j].name) + 1);
                    }
                }

            }

            voicesMap.forEach((values, keys) => {

               labels.push(keys);
               chartValues.push(values);

            });


            colors.push('#999999');
            labels.push('Nicht Abgestimmt');

            for (var i = 0; i < chartValues.length; i++) {

                voted += chartValues[i];

            }

            notVoted = totalVoices - voted;
            chartValues.push(notVoted);

            voicesMap.set('Nicht Abgestimmt', notVoted);

            dmbChart(160, 160, 125, 60, chartValues, colors, labels, 0);
            createLegendTable();
            createLegendTableHeader();

           if(voiceList.voices.length > 0) {

                h2.innerHTML = "Übersicht über die Stimmen";
                createDetailTable();
                createDetailTableHeader();
           }

        }
    };

    xobj.send(null);
}

main();

function main() {

    loadVoiceJSON();
}

function createLegendTable() {

    voicesMap.forEach((values, keys) => {
        (function () {

            var row = table.insertRow(countRow);
            var cellRectangle = row.insertCell(0);
            var cellCLName = row.insertCell(1);
            var cellAmount = row.insertCell(2);
            var cellPercentage = row.insertCell(3);



            if (count < voicesMap.size - 1) {
                var colorCL = consensusLevelList['consensusLevels'][count].color;
            }
            else {
                var colorCL = '#999999';
            }


            var idCanvas = keys + countRow;
            cellRectangle.innerHTML = '<canvas id="' + idCanvas + '"></canvas>';
            drawingTheRectangle(colorCL, idCanvas);


            countRow++;

            cellCLName.setAttribute('class', 'conName');
            cellCLName.innerHTML = keys;
            cellAmount.innerHTML = values;
            var num = (values / totalVoices)*100;
            cellPercentage.innerHTML = num.toFixed(2);

            count++;
        }());
    });
}

function createLegendTableHeader() {
    var row = table.insertRow(0);
    var cell = row.insertCell(0);
    var cellName = row.insertCell(1);
    var cellAmount = row.insertCell(2);
    var cellPercentage = row.insertCell(3);

    cell.setAttribute('class', 'tableHead');
    cellName.setAttribute('class', 'tableHead');
    cellAmount.setAttribute('class', 'tableHead');
    cellPercentage.setAttribute('class', 'tableHead');
    cell.innerHTML = '';
    cellName.innerHTML = "Legende";
    cellAmount.innerHTML = "Anzahl";
    cellPercentage.innerHTML = "Anteil in %";
}

function createDetailTableHeader() {
    var row = detailTable.insertRow(0);
    var cellMemberName = row.insertCell(0);
    var cellExplanation = row.insertCell(1);

    cellMemberName.setAttribute('class', 'detailTableHead');
    cellExplanation.setAttribute('class', 'detailTableHead');
    cellMemberName.innerHTML = "Name";
    cellExplanation.innerHTML = "Begründung";
}

function createDetailTable() {
    for (var i = 0; i < voiceList.voices.length; i++) {

        (function () {
            var row = detailTable.insertRow(0);
            var cellMemberName = row.insertCell(0);
            var cellExplanation = row.insertCell(1);

            cellMemberName.setAttribute('class', 'detailTable');
            cellExplanation.setAttribute('class', 'detailTable');

            var clId = voiceList['voices'][i].consensusLevel;
            var colorOfConsensusLevel;
            for (var j = 0; j < consensusLevelList.consensusLevels.length; j++) {
                if (consensusLevelList['consensusLevels'][j].id === clId) {
                    colorOfConsensusLevel = consensusLevelList['consensusLevels'][j].color;
                }
            }
            cellMemberName.style.backgroundColor = colorOfConsensusLevel;
            cellMemberName.innerHTML = voiceList['voices'][i].member;

            if (voiceList['voices'][i].explanation === "") {
                cellExplanation.innerHTML = "-";
            }
            else {
                cellExplanation.innerHTML = voiceList['voices'][i].explanation;
            }

        }());
    }
}

function drawingTheRectangle(colorCL, idCanvas) {
    var legendCanvas = document.getElementById(idCanvas);
    legendCanvas.width = 25;
    legendCanvas.height = 25;
    var legCan = legendCanvas.getContext("2d");
    legCan.fillStyle = colorCL;
    legCan.fillRect(0, 0, 25, 25);
}

function dmbChart(cx, cy, radius, arcwidth, values, colors, labels, selectedValue) {
    var tot = 0;
    var accum = 0;
    var PI = Math.PI;
    var PI2 = PI * 2;
    var offset = -PI / 2;
    ctx.lineWidth = arcwidth;
    for (var i = 0; i < values.length; i++) { tot += values[i]; }

    for (var i = 0; i < values.length; i++) {
        ctx.beginPath();
        ctx.arc(cx, cy, radius,
            offset + PI2 * (accum / tot),
            offset + PI2 * ((accum + values[i]) / tot)
        );
        ctx.strokeStyle = colors[i];
        ctx.stroke();
        accum += values[i];
    }
    var innerRadius = radius - arcwidth - 35;
    ctx.beginPath();
    ctx.arc(cx, cy, innerRadius, 0, PI2);
    ctx.fillStyle = 'transparent';
    ctx.fill();
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillStyle = 'black';
    ctx.font = (innerRadius) + 'px verdana';
    ctx.fillText(voted + '/' + totalVoices, cx, cy);
    ctx.font = (innerRadius / 4) + 'px verdana';
}
