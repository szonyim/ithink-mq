(function () {
    var input = document.getElementById('messageSearchInput');
    var searchButton = document.getElementById('messageSearchButton');
    if (!input) {
        return;
    }
    var timer = null;

    function performSearch() {
        var term = input.value;
        var url = '/messages/search?query=' + encodeURIComponent(term) + '&page=0';
        fetch(url)
            .then(function (response) {
                return response.text();
            })
            .then(function (html) {
                var container = document.getElementById('messagesTableContainer');
                if (container) {
                    container.outerHTML = html.trim();
                }
            });
    }

    input.addEventListener('input', function () {
        clearTimeout(timer);
        timer = setTimeout(performSearch, 300);
    });

    if (searchButton) {
        searchButton.addEventListener('click', function () {
            clearTimeout(timer);
            performSearch();
        });
    }
})();

(function () {
    var loadForm = document.getElementById('loadForm');
    var loadButton = document.getElementById('loadButton');
    if (!loadForm || !loadButton) {
        return;
    }
    loadForm.addEventListener('submit', function () {
        loadButton.disabled = true;
        loadButton.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Loading...';
    });
})();

(function () {
    var select = document.getElementById('connectionProfileSelect');
    if (!select) {
        return;
    }

    function setValue(fieldId, value) {
        var field = document.getElementById(fieldId);
        if (field) {
            field.value = value || '';
        }
    }

    select.addEventListener('change', function () {
        var option = select.options[select.selectedIndex];
        if (!option || !option.value) {
            return;
        }
        setValue('host', option.dataset.host);
        setValue('port', option.dataset.port);
        setValue('queueManager', option.dataset.queueManager);
        setValue('channel', option.dataset.channel);
        setValue('queue', option.dataset.queue);
        setValue('user', option.dataset.username);
        setValue('password', option.dataset.password);
    });
})();
