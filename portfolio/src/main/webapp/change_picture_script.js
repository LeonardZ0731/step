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
var index = 1;
/**
 * Collect all images in the gallery.html of class "landscape" and make the ith photo visible
 * The function has a precondition that the input i must be an integer between 1 and 16.
 */
function showImg(i) {
    if (i < 1 || i > 16 || (!Number.isInteger(i))) {
        throw "Input is illegal!";
    }
    var images = document.getElementsByClassName("landscape");
    for (var j = 0; j < images.length; j++) {
        if (i === (j + 1)) {
            images[j].style.display = "block";
        } else {
            images[j].style.display =  "none";
        }
    }
    var initial = document.getElementById("initial");
    initial.style.display = "none";
}

function nextImg() {
    index += 1;
    if (index > 16) {
        index = 1;
    }
    showImg(index);
}

function prevImg() {
    index -= 1;
    if (index === 0) {
        index = 16;
    }
    showImg(index);
}