// Starter JavaScript for disabling form submissions if there are invalid fields
function setValidationListener() {
    'use strict';
    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    var forms = document.getElementsByClassName('needs-validation');
    // Loop over them and prevent submission
    var validation = Array.prototype.filter.call(forms, function(form) {
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
function setDatePicker(elem) {
    var options={
        format: 'yyyy-mm-dd',
        todayHighlight: true,
        autoclose: true,
        orientation: 'bottom left',
        language: $('#datePickerLang').text()
    };
    elem.datepicker(options);
}

// operation type radiobuttons
function setOperationTypeListener(async){
    $("input[name=operationType]:radio").change(function() {
        var inAcc = $('#inAccount');
        var outAcc = $('#outAccount');
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
    $('#categoryNameBlankValue').siblings().remove();
    var selectTag = $('#category');
    if (catType == 'TRANSFER') {
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
    var inElem = $('#inAccount');
    var outElem = $('#outAccount');
    var catVal = $('#category').val();
    var catText = $('#category option:selected').text();
    var tempVal;
    if (inElem.val() != '' && catVal != '') {
        tempVal = inElem.val();
        $('#income').click();
        inElem.val(tempVal);
    }
    if (outElem.val() != '' && catVal != '') {
        tempVal = outElem.val();
        $('#outcome').click();
        outElem.val(tempVal);
    }
    if (catVal == '') {
        $('#transfer').click();
    }
    $("#category option").filter(function() {
        return this.text == catText;
    }).attr('selected', true);
}

// Creates operation table and pagination links
function getOperationsPage(page, size, from, till) {
    var data;
    if (from == null || from == '' || till == null || till == '') {
        data = {page: page, size: size};
    } else {
        data = {page: page, size: size, from: from, till: till};
    }
    $.ajax({
        url: '/operations/json',
        data: data,
        success: function (result) {
            fillTable(result.operations);
            $('#pagination').empty();
            if (result.totalPages > 1) {
                fillPaginator(result.currentPage, result.totalPages, size, from, till);
            }
        }
    });
}

// Builds operation table
function fillTable(operations) {
    $('#operationTable').empty();
    var table = $('#operationTableSnippet').clone(true).removeAttr('id');
    $.each(operations,
        function addOperationRow(key, value) {
            var redirectUrl = '&redirect=/operations';
            var inAccount = value.inAccount;
            var outAccount = value.outAccount;
            var category = value.category;
            var sum = value.sum;
            var tr = $('<tr>');
            tr.append($('<td>').attr('align', 'right').text(value.date));
            var outAccountCell = $('<td>').attr('align', 'right');
            if (isNotNull(outAccount)) {
                outAccountCell.text(outAccount.name);
            }
            tr.append(outAccountCell);
            var inAccountCell = $('<td>').attr('align', 'right');
            if (isNotNull(inAccount)) {
                inAccountCell.text(inAccount.name);
            }
            tr.append(inAccountCell);
            var categoryCell = $('<td>').attr('align', 'right');
            if (isNotNull(category)) {
                categoryCell.text(category.name);
            }
            tr.append(categoryCell);
            var formattedSum = sum.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
            if (isNotNull(outAccount) & isNotNull(category)) {
                tr.append($("<td>").attr('align', 'right').addClass('text-danger').text('-' + formattedSum));
            } else if (isNotNull(inAccount) & isNotNull(category)) {
                tr.append($('<td>').attr('align', 'right').addClass('text-success').text('+' + formattedSum));
            } else {
                tr.append($('<td>').attr('align', 'right').text(formattedSum));
            }
            var editLink = $('<a>')
                .attr('href', '/operations/update?id=' + value.id + redirectUrl)
                .addClass("small")
                .text($('#edit-word').text());
            var deleteLink = $('<a>')
                .attr('href', '/operations/delete?id=' + value.id + redirectUrl)
                .attr('onclick', 'return confirm("' + $('#deleteConfirm').html() +  '");')
                .addClass("small")
                .text($('#delete-word').text());
            var editCell = $('<td>').attr('align', 'right').append(editLink).append(' ').append(deleteLink);
            tr.append(editCell);

            table.find('tbody').append(tr);
        });
    $('#operationTable').append(table);
}

function isNotNull(value) {
    return value != undefined & value != 'null';
}

// Fills pagination links
function fillPaginator(cur, total, size, from, till) {
    var visiblePages = 10;
    var methodEnding = size + ',\'' + from + '\',\'' + till +'\'';
    var prevLi = $('#curPage').clone(true).removeAttr('id');
    if (cur == 0){
        prevLi.addClass('disabled');
    } else {
        prevLi.removeClass('disabled');
    }
    prevLi.find('a').attr('onclick', 'getOperationsPage(' + (cur - 1) + ',' + methodEnding + ')');
    prevLi.find('a').html('<<');
    $('#pagination').append(prevLi);

    var firstVisiblePage = cur > visiblePages - 1 ? cur - (visiblePages - 1) : 0;
    var lastVisiblePage = total < firstVisiblePage + visiblePages ? total : firstVisiblePage + visiblePages;

    for (var i = firstVisiblePage; i < lastVisiblePage; i++){
        var curLi = $('#curPage').clone(true).removeAttr('id');
        if (i == cur) {
            curLi.addClass('active');
        }
        curLi.find('a').attr('onclick', 'getOperationsPage(' + i + ', ' + methodEnding + ')');
        curLi.find('a').html(i + 1);
        $('#pagination').append(curLi);
    }

    var nextLi = $('#curPage').clone(true).removeAttr('id');
    if (cur == total - 1){
        nextLi.addClass('disabled');
    } else {
        nextLi.removeClass('disabled');
    }
    nextLi.find('a').attr('onclick', 'getOperationsPage(' + (cur + 1) + ', ' + methodEnding + ')');
    nextLi.find('a').html('>>');
    $('#pagination').append(nextLi);
}

// Chart building
function buildChart() {
    $.ajax({
        url: '/operations/dailyvolumes/json',
        success: function (result) {
                config = {
                    data: result,
                    xkey: 'date',
                    ykeys: ['outcome', 'income'],
                    labels: [$('#incomeChartText').text(), $('#outcomeChartText').text()],
                    fillOpacity: 0.5,
                    hideHover: 'auto',
                    behaveLikeLine: true,
                    resize: true,
                    pointFillColors:['#ffffff'],
                    pointStrokeColors: ['black'],
                    lineColors:['red', 'green']
                };
            config.element = 'chart';
            Morris.Area(config);
        }
    });
}

// Confirmation window for delete action
function confirmWindow() {
    return confirm($('#deleteConfirm').text());
}