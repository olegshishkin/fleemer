// Starter JavaScript for disabling form submissions if there are invalid fields
'use strict';

var chartConfig = {
    element: 'chart',
    xkey: 'date',
    ykeys: ['income', 'outcome'],
    fillOpacity: 0.5,
    labels: [$('#income-chart-text').text(), $('#outcome-chart-text').text()],
    hideHover: 'auto',
    behaveLikeLine: true,
    resize: true,
    pointFillColors: ['#ffffff'],
    pointStrokeColors: ['black'],
    lineColors: ['green', 'red'],
    pointSize: 3
};

if ($('#chart').length > 0) {
    var chart = Morris.Area(chartConfig);
}

function preloadImages() {
    var images = [];
    function preload() {
        for (var i = 0; i < arguments.length; i++) {
            images[i] = new Image();
            images[i].src = arguments[i];
        }
    }
    preload(
        "/static/images/logo_full.png",
        "/static/images/favicon.ico"
    );
}

function setValidationListener() {
    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    var forms = document.getElementsByClassName('needs-validation');
    // Loop over them and prevent submission
    Array.prototype.filter.call(forms, function(form) {
        form.addEventListener('submit', function(event) {
            if (form.checkValidity() === false) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
}

// Set bootstrap datepicker
function setDatePicker() {
    var options={
        format: 'yyyy-mm-dd',
        todayHighlight: true,
        autoclose: true,
        orientation: 'bottom left',
        language: $('#date-picker-lang').text()
    };
    $(arguments).each(function (i, obj) {
        $(obj).datepicker(options);
    });
}

// operation type radiobuttons
function setOperationTypeListener(async){
    $("input[name=operationType]:radio").change(function() {
        var inAcc = $('#in-account');
        var outAcc = $('#out-account');
        var category = $('#category');
        if ($('#outcome').prop('checked')) {
            fillCategories('OUTCOME', async);
            inAcc.prop('selectedIndex', 0).prop('disabled', true).prop('required', false);
            outAcc.prop('disabled', false).prop('required', true);
            category.prop('disabled', false).prop('required', true);
        } else if ($('#income').prop('checked')) {
            fillCategories('INCOME', async);
            outAcc.prop('selectedIndex', 0).prop('disabled', true).prop('required', false);
            inAcc.prop('disabled', false).prop('required', true);
            category.prop('disabled', false).prop('required', true);
        } else if ($('#transfer').prop('checked')) {
            fillCategories('TRANSFER', async);
            category.prop('selectedIndex', 0).prop('disabled', true).prop('required', false);
            inAcc.prop('disabled', false).prop('required', true);
            outAcc.prop('disabled', false).prop('required', true);
        }
    })
}

function fillCategories(catType, async) {
    $('#category-name-blank-value').siblings().remove();
    var selectTag = $('#category');
    if (catType === 'TRANSFER') {
        return;
    }
    $.ajax({
        url: '/categories/json',
        data: { type: catType },
        async: async,
        success: function(result){
            $.each(result,
                function (key, value) {
                    selectTag.append($('<option>').attr('value', value.id).text(value.name));
                });
        }
    });
}

function prepareOperationEditForm() {
    var inElem = $('#in-account');
    var outElem = $('#out-account');
    var catVal = $('#category').val();
    var catText = $('#category option:selected').text();
    var tempVal;
    if (inElem.val() !== '' && catVal !== '') {
        tempVal = inElem.val();
        $('#income').click();
        inElem.val(tempVal);
    }
    if (outElem.val() !== '' && catVal !== '') {
        tempVal = outElem.val();
        $('#outcome').click();
        outElem.val(tempVal);
    }
    if (catVal === '') {
        $('#transfer').click();
    }
    $("#category option").filter(function() {
        return this.text === catText;
    }).attr('selected', true);
}

// Set listener on 'from'-date, 'till'-date and page volume at 'operations' list page.
function setOnChangeListeners() {
    $.each(arguments, function (i, obj) {
        $(obj).change(function () {
            getOperationsPage();
        })
    });
}

// Creates operation table and pagination links
function getOperationsPage(pageNum) {
    var size = $('#page-volume').val();
    if (size === null || size === '') {
        return;
    }
    var from = $('#from-date').val();
    var till = $('#till-date').val();
    var data = {
        page: pageNum === undefined || pageNum === null ? 0 : pageNum,
        size: size,
        from: from === null || from === '' ? null : from,
        till: till === null || till === '' ? null : till
    };
    $.ajax({
        url: '/operations/json',
        data: data,
        success: function (result) {
            fillTable(result.operations);
            $('#pagination').empty();
            if (result.totalPages > 1) {
                fillPaginator(result.currentPage, result.totalPages, size, from, till);
            }
        },
        error: function () {
            fillTable(null);
            $('#pagination').empty();
        }
    });
}

// Builds operation table
function fillTable(operations) {
    $('#operation-table').empty();
    var table = $('#operation-table-snippet').clone(true).removeAttr('id');
    var editIconElem = $('#edit-icon').html();
    var trashIconElem = $('#trash-icon').html();
    var curDate;
    $.each(operations,
        function addOperationRow(key, value) {
            var redirectUrl = '&redirect=/operations';
            var inAccount = value.inAccount;
            var outAccount = value.outAccount;
            var category = value.category;
            var sum = value.sum;
            var existentAccount;
            var tr = $('<tr>').addClass('text-dark');
            var leftSideCell = $('<td>').addClass('text-right fit');
            var arrowCell;
            var rightSideCell = $('<td>').addClass('text-left fit');
            var isOutAccountExist = isNotNullAndNotUndefined(outAccount);
            var isInAccountExist = isNotNullAndNotUndefined(inAccount);
            var isCategoryExist = isNotNullAndNotUndefined(category);
            var formattedSum = sum.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
            var sumCell = $('<span>');
            if (curDate !== value.date) {
                curDate = value.date;
                var dateTr = $('<tr>').addClass('text-dark text-center');
                var dateTd = $('<td>').addClass('text-muted border-bottom');
                dateTd.attr('colspan', 4);
                dateTd.text(curDate);
                dateTr
                    .append($('<td>'))
                    .append(dateTd)
                    .append($('<td>'));
                table.find('tbody').append(dateTr);
            }
            if (isOutAccountExist && isCategoryExist) {
                leftSideCell.text(outAccount.name.slice(0,30));
                arrowCell = $('<td>')
                    .addClass('text-center fit')
                    .append($('#arrow-right i').clone(true).addClass('text-danger'));
                rightSideCell.text(category.name.slice(0,30));
                sumCell.addClass('text-danger').text('-' + formattedSum);
                existentAccount = outAccount;
            } else if (isInAccountExist && isCategoryExist) {
                leftSideCell.text(inAccount.name.slice(0,30));
                arrowCell = $('<td>')
                    .addClass('text-center fit')
                    .append($('#arrow-left i').clone(true).addClass('text-success'));
                rightSideCell.text(category.name.slice(0,30));
                sumCell.addClass('text-success').text('+' + formattedSum);
                existentAccount = inAccount;
            } else {
                leftSideCell.text(outAccount.name.slice(0,30));
                arrowCell = $('<td>')
                    .addClass('text-center fit')
                    .append($('#arrow-right i').clone(true).addClass('text-muted'));
                rightSideCell.text(inAccount.name.slice(0,30));
                sumCell.text(formattedSum);
                existentAccount = outAccount;
            }
            tr.append($('<td>'))
                .append(leftSideCell)
                .append(arrowCell)
                .append(rightSideCell);
            sumCell.append('&nbsp;').append(getCurrencyElem(existentAccount.currency)).append('&nbsp;');
            var editLink = $('<a>').attr('href', '/operations/update?id=' + value.id + redirectUrl);
            editLink.html(editIconElem);
            var spanEditElem = $('<span>').append(editLink);
            var deleteLink = $('<a>')
                .attr('href', '/operations/delete?id=' + value.id + redirectUrl)
                .attr('onclick', 'return confirm("' + $('#delete-confirm').html() +  '");');
            deleteLink.html(trashIconElem);
            var spanDeleteElem = $('<span>').append(deleteLink);
            var editCell = $('<td>')
                .addClass('text-right fit')
                .append(sumCell)
                .append('&nbsp;')
                .append('&nbsp;')
                .append($('<span>').append(spanEditElem).append('&nbsp;').append(spanDeleteElem));
            tr.append(editCell).append($('<td>'));
            table.find('tbody').append(tr);
        });
    $('#operation-table').append(table);
    showTable();
}

function getCurrencyElem(currency) {
    switch (currency) {
        case 'USD':
            return $('#dollar i').clone(true).removeAttr('id');
        case 'EUR':
            return $('#euro i').clone(true).removeAttr('id');
        case 'RUB':
            return $('#ruble i').clone(true).removeAttr('id');
    }
}

function isNotNullAndNotUndefined(value) {
    return value !== undefined && value !== null;
}

// Fills pagination links
function fillPaginator(cur, total, size, from, till) {
    var visiblePages = 10;
    var methodEnding = size + ',\'' + from + '\',\'' + till +'\'';
    var prevLi = $('#current-page').clone(true).removeAttr('id');
    if (cur === 0){
        prevLi.addClass('disabled');
        prevLi.find('a').removeClass('text-dark');
    } else {
        prevLi.removeClass('disabled');
        prevLi.find('a').addClass('text-dark');
    }
    prevLi.find('a').attr('onclick', 'getOperationsPage(' + (cur - 1) + ',' + methodEnding + ')').attr('href', '#');
    prevLi.find('a').html('<<');
    $('#pagination').append(prevLi);

    var firstVisiblePage = cur > visiblePages - 1 ? cur - (visiblePages - 1) : 0;
    var lastVisiblePage = total < firstVisiblePage + visiblePages ? total : firstVisiblePage + visiblePages;

    for (var i = firstVisiblePage; i < lastVisiblePage; i++){
        var curLi = $('#current-page').clone(true).removeAttr('id');
        if (i === cur) {
            curLi.addClass('active');
        }
        curLi.find('a').attr('onclick', 'getOperationsPage(' + i + ', ' + methodEnding + ')').attr('href', '#');
        curLi.find('a').addClass('text-dark');
        curLi.find('a').html(i + 1);
        $('#pagination').append(curLi);
    }

    var nextLi = $('#current-page').clone(true).removeAttr('id');
    if (cur === total - 1){
        nextLi.addClass('disabled');
        nextLi.find('a').removeClass('text-dark');
    } else {
        nextLi.removeClass('disabled');
        nextLi.find('a').addClass('text-dark');
    }
    nextLi.find('a').attr('onclick', 'getOperationsPage(' + (cur + 1) + ', ' + methodEnding + ')').attr('href', '#');
    nextLi.find('a').html('>>');
    $('#pagination').append(nextLi);
}

// Set listener on dates changing event
function setChartDateRangeListener() {
    $.each(arguments, function (i, obj) {
        $(obj).change(function () {
            refreshChart();
        })
    });
}

// Refresh chart
function refreshChart() {
    var chartDateRangeElem = $('#chart-date-range');
    var chartElem = $('#chart');
    var loaderElem = $('#loader-container');
    chartElem.prop('hidden', true);
    chartDateRangeElem.prop('hidden', true);
    loaderElem.prop('hidden', false);

    var from = $('#from-date').val();
    var till = $('#till-date').val();
    var data = {
        from: from === null || from === '' ? null : from,
        till: till === null || till === '' ? null : till
    };
    $.ajax({
        url: '/operations/dailyvolumes/json',
        data: data,
        success: function (result) {
            chartElem.prop('hidden', false);
            chartDateRangeElem.prop('hidden', false);
            loaderElem.prop('hidden', true);
            chart.setData(result);
        }
    });
}

// Confirmation window for delete action
function confirmWindow() {
    return confirm($('#delete-confirm').text());
}

function showTable() {
    $('table').removeAttr('hidden');
    $('#loader-container').remove();
}

function exportButtonClick() {
    $('#message').empty();
    $('#export-btn').addClass('active');
    $('#import-btn').removeClass('active');
    $('#import-form').attr('hidden', true);
    $('#export-form').attr('hidden', false);
}

function importButtonClick() {
    $('#from-date').val('');
    $('#till-date').val('');
    $('#message').empty();
    $('#export-btn').removeClass('active');
    $('#import-btn').addClass('active');
    $('#import-form').attr('hidden', false);
    $('#export-form').attr('hidden', true);
}

function beforPageForwardCountDown(elem) {
    var t = 10;
    setInterval(function () {
        if (t <= 0) {
            window.location = elem.attr('href');
        } else {
            $('#delay').text(t--);
        }
    }, 1000);
}