(function () {
    var purgeButton = document.getElementById('purgeMessagesButton');
    if (!purgeButton) {
        return;
    }
    purgeButton.addEventListener('click', function () {
        if (!confirm('Are you sure you want to delete all messages?')) {
            return;
        }
        fetch('/messages/purge', {method: 'POST'})
            .then(function () {
                window.location.href = '/';
            });
    });
})();

(function () {
    var loadTestDataButton = document.getElementById('loadTestDataButton');
    if (!loadTestDataButton) {
        return;
    }
    loadTestDataButton.addEventListener('click', function () {
        loadTestDataButton.disabled = true;
        fetch('/test-data/load', {method: 'POST'})
            .then(function (response) {
                return response.text().then(function (text) {
                    alert(text);
                });
            })
            .catch(function () {
                alert('An error occurred while loading test data.');
            })
            .finally(function () {
                loadTestDataButton.disabled = false;
            });
    });
})();
