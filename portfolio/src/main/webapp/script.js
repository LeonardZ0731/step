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
