/**
Alexander Baldree
Max Jankowski
Aftabur Rahman
Jordan Dardar

Green team - critique fixes 10-8-26 
Added by Max 
*/
 
 
 // adding call to this from all pages to check if the user is logged in. 
 // if so then it adds 'my account' to header adn remves the unneeded register and login buttons 
(function () {
    "use strict";

    var HOME_PAGE = "post_login.html";

    function currentPage() {
        var name = window.location.pathname.split("/").pop();
        return name || "index.html";
    }

    function updateNav() {
        var nav = document.querySelector('nav[aria-label="Main navigation"]');
        var list = nav && nav.querySelector("ul");
        if (!list) return;

        // already there (e.g. script included twice): nothing to do
        if (list.querySelector('a[href="' + HOME_PAGE + '"]')) return;

        // drops the REGISTER / LOGIN, remembering where the first one was. while the register.html page keeps the register link out of a  li 
        var spot = null;
        Array.prototype.slice.call(list.querySelectorAll("a")).forEach(function (link) {
            var href = link.getAttribute("href");
            if (href !== "login.html" && href !== "register.html") return;
            var item = link;
            while (item.parentNode && item.parentNode !== list) item = item.parentNode;
            if (item.parentNode !== list) return;
            if (!spot) spot = item.nextSibling;
            list.removeChild(item);
        });

        var item = document.createElement("li");
        var link = document.createElement("a");
        link.href = HOME_PAGE;
        link.textContent = "MY ACCOUNT";
        link.id = "userHomeLink";
        if (currentPage() === HOME_PAGE) {
            link.className = "active";
            link.setAttribute("aria-current", "page");
        }
        item.appendChild(link);

        if (spot && spot.parentNode === list) list.insertBefore(item, spot);
        else list.appendChild(item);
    }

    function start() { // created with the assistance of gemini ai and cluade to function properly. I was in a bit of a time crunch 
        fetch("current-user", {
            method: "GET",
            credentials: "same-origin",
            cache: "no-store",
            headers: { "Accept": "application/json" }
        })
            .then(function (response) {
                return response.json().then(function (data) {
                    return response.ok && data && data.ok;
                });
            })
            .then(function (loggedIn) { if (loggedIn) updateNav(); })
            .catch(function () { /* server unreachable: leave the public nav as is */ });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", start);
    } else {
        start();
    }
})();
