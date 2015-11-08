var phoneRegex = /^\(?([0-9]{2,3})\)?[- ]?([0-9]{3})[- ]?([0-9]{4})$/i;
var emailRegex = /(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/i;
var passwordRegex = (/^(?=.*\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,}$/i);
var currentPage = '';

window.onload = function () {
    $(window).bind("hashchange", goToPage());
    initializeFields();
    $.when(isAdmin()).done(function (isAdmin) {
        updateTabs(isAdmin === 'true');
        //populate
    });
    initializeButtons();
    getItemsToManage();
    initButton('login');
    initButton('manage');
    initButton('add');
    initButton('users');
};

function notAdmin() {
    alert("You have no permition!\nPlease login as admin");
}

$(document).ready(function () {
    if (window.location.hash === '') {
        window.location.hash = "#home";
    } else {
        $(window).trigger("hashchange");
    }
});

function updateActiveClass(currATag) {
    $(currATag).parent().addClass("active").siblings(".active").removeClass("active");
}

function updateTabs(isLoggedIn) {
    if (isLoggedIn) {
        $("a[data-lang='new-post']").parent().hide();
        $("a[data-lang='login']").parent().hide();
        $("a[data-lang='my-posts']").parent().show();
        $("a[data-lang='logout']").parent().show();

    } else {
        $("a[data-lang='new-post']").parent().show();
        $("a[data-lang='login']").parent().show();
        $("a[data-lang='my-posts']").parent().hide();
        $("a[data-lang='logout']").parent().hide();
        $("#my > div > .container").empty();
    }
}

function initializeButtons() {
    initLoginButton();
    initCreatePianoButton();
    initCreateBenchButton();
    $('[name="logout-button"]').click(function (e) {
        e.preventDefault();
        $.ajax({
            url: "api/users/logout",
            dataType: 'json',
            success: function () {
                updateTabs(false);
                $("#my-posts-container").empty();
                $("a[href='#posts']").click();
            }
        });
    });
}

function initCreatePianoButton() {
    $("#create-piano-button").click(function (e) {
        e.preventDefault();
        $.when(isAdmin()).done(function (isLoggedInAsAdmin) {
            if (isLoggedInAsAdmin === 'true') { //damn js.. should be cleaner?
                $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    url: "api/catalog/pianos",
                    ///should be fixed - accessing the DOM instead accessing the form
                    data: '{"isUpright":' + getBooleanFromRadio("create-piano-isupright") + ',"model":"' + $("#create-piano-model").val() + '","manufacturer":"' + $("#create-piano-manufacturer").val() + '","country":"' + $("#create-piano-country").val() + '","size":' + $("#create-piano-size").val() + ',"color":"' + $("#create-piano-color").val() + '","finish":"' + $("#create-piano-finish").val() + '","year":' + $("#create-piano-year").val() + ',"isNew":' + getBooleanFromRadio("create-piano-isnew") + ',"img":"http:"}',
                    dataType: 'json',
                    type: 'POST',
                    success: function () {
                        getBooleanFromRadio("create-piano-isupright") ? getUprightPianosToManage() : getGrandPianosToManage();
                    },
                    error: function (data) {
                        alert("error!! " + data.responseText);//TODO
                    }
                });
            } else {
                notAdmin();
            }
        });
    });
}

function initCreateBenchButton() {
    $("#create-bench-button").click(function (e) {
        e.preventDefault();
        $.when(isAdmin()).done(function (isLoggedInAsAdmin) {
            if (isLoggedInAsAdmin === 'true') { //damn js.. should be cleaner?
                $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    url: "api/catalog/benches",
                    ///should be fixed - accessing the DOM instead accessing the form
                    //data: '{"isUpright":' + getBooleanFromRadio("create-piano-isupright") + ',"model":"' + $("#create-piano-model").val() + '","manufacturer":"' + $("#create-piano-manufacturer").val() + '","country":"' + $("#create-piano-country").val() + '","size":' + $("#create-piano-size").val() + ',"color":"' + $("#create-piano-color").val() + '","finish":"' + $("#create-piano-finish").val() + '","year":' + $("#create-piano-year").val() + ',"isNew":' + getBooleanFromRadio("create-piano-isnew") + ',"img":"http:"}',
                    data: '{"model":"' + $("#create-bench-model").val() + '","color":"' + $("#create-bench-color").val() + '","fabric":"' + $("#create-bench-fabric").val() + '","isAdjustable":' + getBooleanFromRadio("create-bench-isadjustable") + ',"img":"http:"}',
                    dataType: 'json',
                    type: 'POST',
                    success: function () {
                        getBenchesToManage();
                    },
                    error: function (data) {
                        alert("error!! " + data.responseText);//TODO
                    }
                });
            } else {
                notAdmin();
            }
        });
    });
}

function initButton(name) {
    $("a[href$='#" + name + "']").click(function (event) {
        event.preventDefault();
        if (window.location.hash !== name) {// if clicking on a different page then the current page
            window.location.hash = this.hash;
            $('.nav.nav-tabs > li > a[data-lang="' + name + '"]').parent().addClass("active").siblings(".active").removeClass("active");
            goToPage();
//            $("#" + currentPage).fadeOut("fast", function () {
//                currentPage = name;
//                $("#" + name).fadeIn("fast");
//            });
        }
    });
}

function getItemsToManage() {
    getUprightPianosToManage();
    getGrandPianosToManage();
    getBenchesToManage();
}

function getUprightPianosToManage() {
    $.ajax({
        url: "api/catalog/pianos/upright",
        dataType: 'json',
        success: function (data) {
            $("#admin-upright-container > tbody").empty().append(renderListToTable(data, renderPianoRow));
            $("#admin-upright-container > tbody > tr > td:last-child > button").click(function (e) {
                e.preventDefault();
                initDeleteItem($(this).attr("name"), 'pianos', getUprightPianosToManage);
            });
            $("#admin-upright-container > tbody > tr > td:nth-child(8) > button").click(function (e) {
                alert("1");
                e.preventDefault();
                initPromoItem($(this).attr("name"), $(this).attr("data-method"));
            });
        }
    });
    getPromoPianos();
}

function getGrandPianosToManage() {
    $.ajax({
        url: "api/catalog/pianos/grand",
        dataType: 'json',
        success: function (data) {
            $("#admin-grand-container > tbody").empty().append(renderListToTable(data, renderPianoRow));
            $("#admin-grand-container > tbody > tr > td:last-child > button").click(function (e) {
                e.preventDefault();
                initDeleteItem($(this).attr("name"), 'pianos', getGrandPianosToManage);
            });
            $("#admin-grand-container > tbody > tr > td:nth-child(8) > button").click(function (e) {
                e.preventDefault();
                initPromoItem($(this).attr("name"), $(this).attr("data-method"));
            });
        }
    });
    getPromoPianos();
}

function getBenchesToManage() {
    $.ajax({
        url: "api/catalog/benches",
        dataType: 'json',
        success: function (data) {
            $("#admin-benches-container > tbody").empty().append(renderListToTable(data, renderBenchRow));
            $("#admin-benches-container > tbody > tr > td:last-child > button").click(function (e) {
                e.preventDefault();
                initDeleteItem($(this).attr("name"), 'benches', getBenchesToManage);
            });
        }
    });
}

function initDeleteItem(id, whatDB, whatToGet) {
    $(this).attr("disabled", true);
    $.ajax({
        url: "api/catalog/" + whatDB + "/" + id,
        dataType: 'json',
        type: 'DELETE',
        context: this,
        success: function () {
            whatToGet();
        },
        error: function (data) {
            alert("ffffuck!! " + $(this).attr("name"));//TODO
        }
    });
}

function initPromoItem(id, method) {
    $(this).attr("disabled", true);
    $.ajax({
        url: "api/catalog/pianos/promotions/" + id,
        dataType: 'json',
        type: method,
        context: this,
        success: function () {
            updateNotPromotionButton(id);
            getPromoPianos();
            $(this).attr("disabled", false);
        },
        error: function () {
            alert("ffffuck!! " + $(this).attr("name"));//TODO
        }
    });
}

function getPromoPianos() {
    $.ajax({
        url: "api/catalog/pianos/promotions",
        dataType: 'json',
        success: function (promotionPianos) {
            for (var i = 0, len = promotionPianos.length; i < len; i++) {
                updatePromotionButton(promotionPianos[i].id);
            }
        }
    });
}

function renderListToTable(items, renderer) {
    var result;
    for (var i = 0, len = items.length; i < len; i++) {
        result += '<tr>' + renderer(items[i]) + '</tr>';
    }
    return result;
}

function renderPianoRow(piano) {
    return '<td>' + piano.manufacturer + '</td>'
            + '<td>' + piano.model + '</td>'
            + '<td>' + piano.country + '</td>'
            + '<td>' + piano.year + '</td>'
            + '<td>' + piano.size + '</td>'
            + '<td>' + piano.color + '</td>'
            + '<td>' + piano.finish + '</td>'
            + '<td>' + GeneratePromotionButton(piano.id) + '</td>'
            + '<td>' + generateDeleteButton(piano.id) + '</td>';
}

function renderBenchRow(bench) {
    return '<td>' + bench.model + '</td>'
            + '<td>' + bench.color + '</td>'
            + '<td>' + bench.fabric + '</td>'
            + '<td>' + generateVorX(bench.isAdjustable, '') + '</td>'
            + '<td>' + generateDeleteButton(bench.id) + '</td>';
}

function GeneratePromotionButton(id) {
    return '<button name="' + id + '" data-method="POST">'
            + generateVorX(false, '');
    +'</button>';
}

function updatePromotionButton(id) {
    $("#admin-upright-container, #admin-grand-container").find("button[data-method][name='" + id + "']").
            attr("data-method", "DELETE").children().attr("class", "glyphicon glyphicon-ok");
}

function updateNotPromotionButton(id) {
    $("#admin-upright-container, #admin-grand-container").find("button[data-method][name='" + id + "']").
            attr("data-method", "POST").children().attr("class", "glyphicon glyphicon-remove");
}
