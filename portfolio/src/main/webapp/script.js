// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomFact() {
  const fact_1 = 'I grew up in China and my hometown is Shanghai.';
  const fact_2 = 'I am also studying for a Computation and Applied Math Major.'
  const fact_3 = 'I\'ve been playing the piano for 12 years.';
  const fact_4 = 'I have never learned programming before college.';
  const fact_5 = 'My favorite football team is San Francisco 49ers.';
  const fact_6 = 'My favorite soccer team is Manchester United.';
  const fact_7 = 'I really like playing League of Legends, but I\'m not good at it. Sad.';
  const fact_8 = 'I watched a lot of animes and mangas during my spare time.';
  const fact_9 = 'My favorite manga is Naruto and my favorite character in Naruto is Kakashi.';
  const facts =
      [fact_1, fact_2, fact_3, fact_4, fact_5, fact_6, fact_7, fact_8, fact_9];

  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  var para = document.createElement("p");
  var node = document.createTextNode(fact);
  para.appendChild(node);
  factContainer.innerHTML = '';
  factContainer.appendChild(para);
}

function displayComments(maxComments, comments) {
    const commentContainer = document.getElementById("comment-container");
    console.log(maxComments);
    commentContainer.innerHTML = "";
    for (var index = 0; index < comments.length; index++) {
        if (index === maxComments) break;
        var commentString = comments[index].comment;
        var keyString = comments[index].keyString;
        console.log(keyString);
        var para = document.createElement("p");
        var node = document.createTextNode(commentString);
        para.appendChild(node);
        var button = document.createElement("button");
        button.innerHTML = "Like";
        button.type = "button";
        const input = keyString.slice();
        button.addEventListener("click", function () { likeComments(input) });
        commentContainer.appendChild(para);
        commentContainer.appendChild(button);
    };
}

async function fetchCommentsWithStoredLimit() {
    console.log("Try fetch");
    const response = await fetch("/comments");
    const commentResponse = await response.json();
    const comments = commentResponse.commentList;
    var maxComments = parseInt(commentResponse.maxComments, 10);
    if (Number.isNaN(maxComments)) {
        console.log("You are trying to input a non-numeric data, please try again");
        // Set the limit to default value
        maxComments = 10;
    }
    const commentLimitContainer = document.getElementById("comment-limit");
    commentLimitContainer.value = (maxComments.toString());
    displayComments(maxComments, comments);
}

async function fetchCommentsWithInputLimit() {
    var inputLimit = document.getElementById("comment-limit").value;
    if (Number.isNaN(parseInt(inputLimit, 10))) {
        console.log("You are trying to input a non-numeric data, please try again");
        inputLimit = "10";
    }
    const queryURL = "/comments?maximum-comments=" + inputLimit;
    const request = new Request(queryURL, {method: "PUT"});
    const response = await fetch(request);
    fetchCommentsWithStoredLimit();
}

async function deleteComments() {
    console.log("Delete all comments in datastore");
    const request = new Request("/comments", {method: "DELETE"});
    const response = await fetch(request);
    const commentContainer = document.getElementById("comment-container");
    commentContainer.innerHTML = "";
}

async function likeComments(keyString) {
    console.log("Like a certain comment");
    const queryURL = "/comments/like?key=" + keyString;
    const request = new Request(queryURL, {method: "POST"});
    const newResponse = await fetch(request);
}