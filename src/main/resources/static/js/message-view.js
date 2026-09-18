(function () {
    document.addEventListener('click', function (event) {
        var button = event.target.closest('[data-copy-target]');
        if (!button) {
            return;
        }
        var target = document.getElementById(button.getAttribute('data-copy-target'));
        if (!target) {
            return;
        }
        if (!navigator.clipboard) {
            showFeedback(button, 'Copy not supported');
            return;
        }
        navigator.clipboard.writeText(target.textContent)
            .then(function () {
                showFeedback(button, 'Copied!');
            })
            .catch(function () {
                showFeedback(button, 'Copy failed');
            });
    });

    function showFeedback(button, message) {
        var originalLabel = button.textContent;
        button.textContent = message;
        setTimeout(function () {
            button.textContent = originalLabel;
        }, 1500);
    }
})();
