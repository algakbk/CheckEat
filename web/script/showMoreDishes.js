$(function () {
    $('.carousel').carousel({interval: 5000});

    var title = sessionStorage.getItem("title");
    var title = document.getElementById('header');
    title.innerHTML = title;

    getLastUploadsFromServer();

});

function getLastUploadsFromServer() {
    $.ajax({
        type: 'get',
        url: 'dish',
        data: {
            'requestType': 'getDishesOrderedByUploadDate'
        },
        success: function(dishes)
        {
            if(dishes && dishes.length > 0)
                loadLastUploads(dishes);
        }
    });
}

function loadDishes(dishList)
{
    var dishes = document.getElementById('dishes');
    var size = dishList.length;
    var i = 0;
    for(var j = 0; j < 3 && i < size &&  i < 6; j++) {
        var col = document.createElement('div');
        col.classList.add('col-md-4');
        loadDish(col, dishList[i]);
        dishes.appendChild(col);
        i++;
    }
}

function loadDish(element, dish)
{
    var panel = document.createElement('div');
    panel.classList.add('panel', 'panel-default');
    element.appendChild(panel);

    var header = document.createElement('div');
    header.classList.add('panel-heading');
    panel.appendChild(header);
    var dishName = document.createElement('h4');
    dishName.textContent = dish.dishName;
    header.appendChild(dishName);

    var body = document.createElement('div');
    body.classList.add('panel-body');
    if(addImg(body, dish.dishUrl, '100px', '200px'))
        addNewLine(body);
    if(addDetail(body, 'מסעדה:', dish.restName))
        addNewLine(body);
    if(addAddress(body, dish.restCity, dish.restStreet, dish.restStreetNum))
        addNewLine(body);
    if(addDetail(body, 'הועלה בתאריך:', dish.uploadDate))
        addNewLine(body);
    var btn = addButton(body, 'הצג');
    btn.classList.add('alignLeft');
    panel.appendChild(body);
}

//*******************************************************************************************
//Adding Controls
function addImg(element, url, height, width)
{
    var added = false;
    if(url) {
        added = true;
        var img = document.createElement('img');
        img.setAttribute('src', url);
        img.setAttribute('height', height);
        img.setAttribute('width', width);
        element.appendChild(img);
    }
    return added;
}

function addDetail(element, detail, text)
{
    var added = false;
    if(text) {
        added = true;
        var label = addLabel(element, detail);
        label.classList.add('detailStyle');
        addSpace(element);
        addLabel(element, text);
    }
    return added;
}

function addAddress(element, city, street, streetNum)
{
    var added = false;
    if(city) {
        added = true;
        var label = addLabel(element, 'כתובת:');
        label.classList.add('detailStyle');
        addSpace(element);
        addLabel(element, city);
        if(street) {
            addLabel(element, ',');
            addSpace(element);
            addLabel(element, street);
            if(streetNum) {
                addSpace(element);
                addLabel(element, streetNum);
            }
        }
    }
    return added;
}

function addLabel(element, text)
{
    var label = document.createElement('label');
    label.innerHTML = text;
    element.appendChild(label);
    return label;
}

function addLink(element, text, link) {
    var added = false;
    if(link) {
        added = true;
        var label = addLabel(element, text);
        label.classList.add('detailStyle');
        addSpace(element);
        var a = document.createElement('a');
        a.href = link;
        a.innerHTML = "לחץ כאן";
        element.appendChild(a);
    }
    return added;
}

function addButton(element, text) {
    var button = document.createElement('button');
    button.innerHTML = text;
    button.setAttribute('class', 'btn btn-default');
    element.appendChild(button);
    return button;
}

function addNewLine(element) {
    var br = document.createElement('br');
    element.appendChild(br);
}

function addSpace(element) {
    addLabel(element, "&nbsp;");
}

function addIcon(element, _class) {
    var i = document.createElement('i');
    i.className = _class;
    i.setAttribute('aria-hidden', 'true');
    element.appendChild(i);
    return i;
}

function addList(element, text, list) {
    var added = false;
    if (!isEmptyList(list)) {
        added = true;
        var label = addLabel(element, text);
        label.classList.add('detailStyle');
        addSpace(element);
        for (var i = 0; i < list.length; i++) {
            addLabel(element, list[i]);
            if(i < list.length - 1) {
                addLabel(element, ',');
                addSpace(element);
            }
        }
    }
    return added;
}

//*******************************************************************************************
//Validation
function isEmptyList(list) {
    var empty = true;
    if(typeof list !== 'undefined' && list.length > 0) {
        for (var i = 0; i < list.length; i++) {
            if(list[i].length !== 0) {
                empty = false;
                break;
            }
        }
    }
    return empty;
}

function isEmptyString(str) {
    var empty = true;
    if(str && str.length !== 0)
        empty = false;
    return empty;
}