
const pollData_div = document.getElementById("pollData");
const tilte = document.getElementById("title");
const header = document.getElementById("header");
const table = document.getElementById("pollTable");
var pollsJSON;

function loadPollJSON() {
  var xobj = new XMLHttpRequest();

  xobj.onreadystatechange = function () {

    if (xobj.readyState == 4 && xobj.status == '200') {

        pollsJSON = JSON.parse(xobj.responseText);

        header.innerHTML = "Abstimmungen innerhalb des Meetings: " + pollsJSON.name;

        for (var i = 0; i < pollsJSON.polls.length; i++) {

            (function () {
              var row = table.insertRow(i);
              var cellName = row.insertCell(0);
              var pollId = pollsJSON['polls'][i].id;
              var pollName = pollsJSON['polls'][i].title;

              var cellProposal = row.insertCell(1);
              var cellIcon = row.insertCell(2);
              cellName.innerHTML = pollsJSON['polls'][i].title;
              cellName.setAttribute('class', 'firstTable');
              cellProposal.innerHTML = pollsJSON['polls'][i].consensusProposal;
              cellIcon.innerHTML = '<span class="material-icons">launch</span>';
              cellIcon.addEventListener("click", function () {
                openResult(pollId, pollName);
              });

            }());

        }

        createTableHeader();

    }

  };

  xobj.open('GET', '/polls', true);
  xobj.send(null);
}

function main() {

 loadPollJSON();
}

function openResult(pollId, pollName) {

    var location = window.location.href;
    window.open(location + "result?pollId=" + pollId + "&name=" + pollName);
}

function createTableHeader() {
  var row = table.insertRow(0);
  row.setAttribute('class', 'tableHead');
  var cellName = row.insertCell(0);
  var cellProposal = row.insertCell(1);
  var cellIcon = row.insertCell(2);

  cellName.setAttribute('class', 'tableHead');
  cellProposal.setAttribute('class', 'tableHead');
  cellName.innerHTML = "Abstimmung";
  cellProposal.innerHTML = "Konsenssvorschlag";

}

main();