(function () {
    document.addEventListener('submit', function (event) {
        var form = event.target;
        var message = form.getAttribute('data-confirm');
        if (message && !confirm(message)) {
            event.preventDefault();
        }
    }, true);
})();
