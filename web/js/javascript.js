var phoneRegex = /^\(?([0-9]{2,3})\)?[- ]?([0-9]{3})[- ]?([0-9]{4})$/i;
var emailRegex = /(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/i;
var passwordRegex = (/^(?=.*\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,}$/i);
var currentPage = '';

window.onload = function () {
    if (window.location.pathname.indexOf("/admin.html") === -1) {
        $(window).bind("hashchange", goToPage());
        initializeButtons();
        initializeFields();
        getUprightPianos();
        getGrandPianos();
        getBenchesFromServer();
        getPromoPianos();
        getPosts();
        getRecentPosts();

        $.when(isLoggedIn()).done(function (isLogged) {
            updateTabs(isLogged === 'true'); ////damn js.. should be cleaner?
            getMyPosts();
        });

        translateTo("eng");
    }
};

$(document).ready(function () {
    if (window.location.hash === '') {
        window.location.hash = "#home";
    } else {
        $(window).trigger("hashchange");
    }
});

function goToPage() {
    var hash = window.location.hash;
    $('[data-page]').hide();
    $('[data-page="' + hash + '"]').show().parents('[data-page]').show();

    //if(url.length==1) { url=url[0]; } else { return;}
    ////push state
}

function updateActiveClass(currATag) {
    $(currATag).parent().show();
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
    initButton("home");
    initButton("upright");
    initButton("grand");
    initButton("benches");
    initButton("services");
    initButton("posts");
    initButton("login");
    initButton("new-post");
    initButton("my");
    initButton("contact");
    $("#create-post-button").click(function (e) {
        e.preventDefault();
        $.when(isLoggedIn()).done(function (isLoggedIn) {
            if (isLoggedIn === 'true') { //damn js.. should be cleaner?
                $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    url: "api/posts",
                    ///should be fixed - accessing the DOM instead accessing the form
                    data: '{"text":"' + $("#create-post-text").val() + '","name":"' + $("#create-post-name").val() + '","email":"' + $("#create-post-email").val() + '","phone":"' + $("#create-post-phone").val() + '","img":"http:!!!!"}',
                    dataType: 'json',
                    type: 'POST',
                    success: function () {
                        getMyPosts();
                        getRecentPosts();
                        getPosts();
//                        setTimeout(function () {
//                            $("a[href='#posts']").click();
//                        }, 3000);
                    },
                    error: function (data) {
                        alert("ffffuck!! " + data.responseText);//TODO
                    }
                });
            } else {
                $("a[href='#login']").click();
                //show message "need to login"
            }
        });
    });

    initLoginButton();
    $("#signup-button").click(function (e) {
        e.preventDefault();
        $.ajax({
            ///should be fixed - accessing the DOM instead accessing the form
            url: "api/users/user/" + $("#signup-email").val() + "/" + $("#signup-password").val() + "",
            dataType: 'json',
            type: 'POST',
            success: function () {
                login($("#signup-email").val(), $("#signup-password").val());
            },
            error: function (data) {
                alert("signup " + data.responseText);//TODO
            }
        });
    });
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

function initLoginButton() {
    //TODO - manage buttons to be enabled/disabled (by form data)
    $("#login-button").click(function (e) {
        e.preventDefault();
        ///should be fixed - accessing the DOM instead accessing the form
        login($("#login-email").val(), $("#login-password").val());
    });
}

function getBooleanFromRadio(id) {
    return $("#" + id + " > label.active > input").attr("name");
}

function translateTo(language) {
    $.ajax({
        url: 'languages.xml',
        success: function (xml) {
            $(xml).find('translation').each(function () {
                var id = $(this).attr('id');
                var text = $(this).find(language).text();
                $("[data-lang='" + id + "']").contents().filter(function () {
                    return this.nodeType === 3;
                }).remove();
                $("[data-lang='" + id + "']").prepend(text);
                //$("." + id).addClass(id + '_' + language); if we want to add a css for each language
            });
        },
        error: function () {
            alert("page not found");
        }
    });
}

function login(username, password) {
    $.ajax({
        url: "api/users/user/" + username + "/" + password + "",
        dataType: 'json',
        success: function (data) {
            $("a[href='#new-post']").click();
            updateTabs(true);
            getMyPosts();
        },
        error: function (data) {
            alert("login error: " + data.responseText);//TODO
        }
    });
}

function getGrandPianos() {
    getPianosFromServer("grand");
}

function getUprightPianos() {
    getPianosFromServer("upright");
}

function getPianosFromServer(type) {
    $.ajax({
        url: "api/catalog/pianos/" + type,
        dataType: 'json',
        success: function (data) {
            //$("#" + type).append("<p>" + type + "<br>" + JSON.stringify(data, null, 2) + "</p>");
            $("#" + type + "-container").empty().append(renderPianos(data));
        }
    });
}

function getBenchesFromServer() {
    getBenches(function (data) {
        $("#benches-container").empty().append(renderBenches(data));
    });
}

function getPromoPianos() {
    $.ajax({
        url: "api/catalog/pianos/promotions",
        dataType: 'json',
        success: function (data) {
            $("#promotion-container").append(renderPromotionPianos(data));
        }
    });
}

function getPosts() {
    $.ajax({
        url: "api/posts",
        dataType: 'json',
        success: function (data) {
            $("#posts-container").empty().append(renderPosts(data));
        }
    });
}

function getRecentPosts() {
    $.ajax({
        url: "api/posts/recent",
        dataType: 'json',
        success: function (data) {

            var errorString = "Couldn't fetch recent posts";
            $("#recent-posts-container").empty().append(renderErrorModal(errorString));
            $('#myModal').modal('show');
            $("#myModal").on('hidden.bs.modal', function () {
                $(this).data('bs.modal', null);
            });

        },
        error: function () {
            var errorString = "Couldn't fetch recent posts";
            $("#recent-posts-container").empty().append(renderErrorModal(errorString));
            $('#myModal').modal('show');
        }
    });
}

function getMyPosts() {
    $.ajax({
        url: "api/posts/my/",
        dataType: 'json',
        success: function (data) {
            $("#my-posts-container").empty().append(renderPostsMy(data));
            var trashButton = $("#my-posts-container").find(".panel-footer > button");
            trashButton.click(function (e) {
                e.preventDefault();
                $(this).attr("disabled", true);
                $.ajax({
                    url: "api/posts/" + $(this).attr("name"),
                    dataType: 'json',
                    type: 'DELETE',
                    context: this,
                    success: function () {
                        //$(this).parent().parent().parent().remove();
                        getMyPosts();
                        getPosts();
                        getRecentPosts();
                    },
                    error: function (data) {
                        alert("ffffuck!! " + $(this).attr("name"));//TODO
                    }
                });
            });
        }
    });
}

function isLoggedIn() {
    return $.ajax({
        url: "api/users/login",
        dataType: 'json'
    }).pipe(function (data) {
        return data.Login;
    });
}

function isAdmin() {
    return $.ajax({
        url: "api/users/admin",
        dataType: 'json'
    }).pipe(function (data) {
        return data.isAdmin;
    });
}

function renderRecentPosts(posts) {
    var result = '';
    for (var i = 0, len = posts.length; i < len; i++) {
        result += renderRecentPost(posts[i], '');
    }
    return result;
}

function renderPosts(posts) {
    var result = '<div class="row">';
    for (var i = 0, len = posts.length; i < len; i++) {
        result += renderPost(posts[i], '');
    }
    result += '</div>';
    return result;
}

function renderPostsMy(posts) {
    var result = '<div class="row">';
    for (var i = 0, len = posts.length; i < len; i++) {
        result += renderPost(posts[i], generateDeleteFooter(posts[i].id));
    }
    result += '</div>';
    return result;
}

function renderPost(post, footer) {
    var d = new Date(post.date);
    var dateStr = d.getDate() + "/" + (d.getMonth() + 1) + "/" + (d.getFullYear() - 2000);

    var image = post.img !== 'null' ? 'img/posts/no-piano.png' : post.img;
    return '<div class="col-md-4 col-sm-6">'
            + '<div class="panel panel-default">'
            + '<div class="panel-heading">'
            + '<h3 class="panel-title"><span class="glyphicon glyphicon-user">'
            + '</span>' + post.name + '<span class="pull-right">' + dateStr + '</span></h3>'
            + '</div>'
            + '<div class="panel-body">'
            + '<div class="row">'
            + '<div class="col-sm-8">'
            + post.text
            + '</div>'
            + '<div class="col-sm-4">'
            + '<a href="#" class="thumbnail">'
            + '<img src="' + image + '">'
            + '</a>'
            + '</div>'
            + '</div>'
            + ((post.phone === 'null' || post.email === 'null') ? '<div class="top20"></div>' : '')
            + (post.phone === 'null' ? '' : addPhone(post))
            + (post.email === 'null' ? '' : addMail(post))
            + '</div>'
            + footer
            + '</div>'
            + '</div>'
            ;
}

function renderRecentPost(post) {
    var image = post.img !== 'null' ? 'img/posts/no-piano.png' : post.img;
    return '<li class="list-group-item">'
            + '<div class="row">'
            + '<div class="col-xs-2">'
            + '<a href="#" class="thumbnail bottom0">'
            + '<img src="' + image + '">'
            + '</a>'
            + '</div>'
            + '<div class="col-xs-10">' + post.text + '</div>'
            + '</div>'
            + '</li>'
            ;
}

function renderErrorModal(errorString) {

    return  '<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">'
            + '<div class="modal-dialog">'
            + '<div class="modal-content">'
            + '<div class="modal-header">'
            + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
            + '<h4 class="modal-title" id="myModalLabel">Modal title</h4>'
            + '</div>'
            + '</li>'
            + '<div class="modal-body">'
            + errorString
            + '</div>'
            + '<div class="modal-footer">'
            + '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>'
            + '</div>'
            + '</div>'
            + '</div>'
            + '</div>'
            ;

}

function renderBenches(benches) {
    var result = '<div class="row">';
    for (var i = 0, len = benches.length; i < len; i++) {
        result += renderBench(benches[i], '');
    }
    result += '</div>';
    return result;
}


function renderBench(bench, footer) {
    // var d = new Date(post.date);
    //  var dateStr = d.getDay() + "/" + d.getMonth() + "/" + (d.getFullYear() - 2000);
    var image = bench.img !== 'null' ? 'img/posts/no-piano.png' : bench.img;

    return '<div class="col-md-4 col-sm-6">'
            + '<div class="panel panel-default">'
            + '<div class="panel-heading">'
            + '<h3 class="panel-title"><span class="glyphicon glyphicon-certificate">'
            + '</span>' + bench.model + '<span class="pull-right"></span></h3>'
            + '</div>'
            + '<div class="panel-body">'
            + '<div class="row">'
            + '<div class="col-sm-6">'
            + '<p> Color: ' + bench.color + ' </p>'
            + '<p> Fabric: ' + bench.fabric + ' </p>'
            + '<p>' + generateVorX(bench.isAdjustable, 'Adjustable') + '</p>'
            + '</div>'
            + '<div class="col-sm-6">'
            + '<a href="#" class="thumbnail">'
            + '<img src="' + image + '">'
            + '</a>'
            + '</div>'
            + '</div>'
            + '</div>'
            + footer
            + '</div>'
            + '</div>'
            ;
}

function generateVorX(is, text) {
    return '<span class="glyphicon glyphicon-' + (is ? 'ok' : 'remove') + '"> ' + text + '</span>';
}

function renderPromotionPianos(posts) {
    var result = '';
    for (var i = 0, len = posts.length; i < len; i++) {
        result += renderPromotionPiano(posts[i], '');
    }
    return result;
}

function renderPromotionPiano(piano) {
    var image = piano.img !== 'null' ? 'img/posts/no-piano.png' : post.img;
    return '<li class="list-group-item">'
            + '<div class="row">'
            + '<div class="col-xs-2">'
            + '<a href="#" class="thumbnail bottom0">'
            + '<img src="' + image + '">'
            + '</a>'
            + '</div>'
            + '<div class="col-xs-10"><h4>' + piano.manufacturer + ' ' + piano.model + '</h4></div>'
            + '</div>'
            + '</li>'
            ;
}

function renderPianos(Pianos) {
    var result = '<div class="row">';
    for (var i = 0, len = Pianos.length; i < len; i++) {
        result += renderPiano(Pianos[i], '');
    }
    result += '</div>';
    return result;
}


function renderPiano(Piano, footer) {
    var image = 'img/posts/no-piano.png';
    return '<div class="col-md-4 col-sm-6">'
            + '<div class="panel panel-default">'
            + '<div class="panel-heading">'
            + '<h3 class="panel-title"><span class="glyphicon glyphicon-certificate">'
            + '</span>' + Piano.manufacturer + '  ' + Piano.model + '<span class="pull-right"></span></h3>'
            + '</div>'
            + '<div class="panel-body">'
            + '<div class="row">'
            + '<div class="col-sm-6">'
            + '<p> Country: ' + Piano.country + ' </p>'
            + '<p> Year: ' + Piano.year + ' </p>'
            + '<p> Size: ' + Piano.size + ' </p>'
            + '<p> Color: ' + Piano.color + ' </p>'
            + '<p> Finish: ' + Piano.finish + ' </p>'
            + '</div>'
            + '<div class="col-sm-6">'
            + '<a href="#" class="thumbnail">'
            + '<img src="' + image + '">'
            + '</a>'
            + '</div>'
            + '</div>'
            + '</div>'
            + footer
            + '</div>'
            + '</div>'
            ;
}

function addPhone(post) {
    return '<div class="phone">'
            + '<a href="tel:' + post.phone + '">'
            + '<span class="glyphicon glyphicon-earphone" aria-hidden="true">'
            + '</span><span class="glyphicon" aria-hidden="true">' + post.phone + '</span>'
            + '</a>'
            + '</div>'
            ;
}

function addMail(post) {
    return '<div class="mail">'
            + '<a href="mailto:' + post.email + '">'
            + '<span class="glyphicon glyphicon-envelope" aria-hidden="true">'
            + '</span><span class="glyphicon" aria-hidden="true">' + post.email + '</span>'
            + '</a>'
            + '</div>'
            ;
}

function generateDeleteFooter(id) {
    return '<div class="panel-footer">'
            + generateDeleteButton(id) + ' Delete'
            + '</div>';
}

function generateDeleteButton(id) {
    return '<button name="' + id + '">'
            + '<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>'
            + '</button>';
}

function initializeFields() {
    $("#signup-email").keyup(function () {
        validateField("#signup-email", validateEmail($(this).val()));
    });
    $("#signup-password").keyup(function () {
        $("#signup-password-confirm").val('');
        validateField("#signup-password", validatePassword($(this).val()));
    });
    $("#signup-password-confirm").keyup(function () {
        validateField("#signup-password-confirm", $(this).val() === $("#signup-password").val());
    });
}

function validatePhone(inputPhone) {
    return inputPhone.match(phoneRegex);
}

function validateEmail(inputEmail) {
    return inputEmail.match(emailRegex);
}

function validatePassword(inputPassword) {
    return inputPassword.match(passwordRegex);
}

function validateField(field, match) {
    if (match) {
        $(field).parents(".form-group").removeClass("has-error").addClass("has-success");
    } else {
        $(field).parents(".form-group").addClass("has-error").removeClass("has-success");
    }
}
