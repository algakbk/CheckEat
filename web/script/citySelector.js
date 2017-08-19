$(document).ready(function() {
    $(".js-example-basic-single").select2();
});

$(function () {
    if (window.location.href != "http://localhost:8080/index.html" && window.location.href != "http://localhost:8080/")
        getCitiesFromServer();
   /* else {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(setNearbyRestaurantsHomePage);
        }
    }*/
});
/*
function setNearbyRestaurantsHomePage(position) {
    var currCity = findCurrCity(position);
    sessionStorage.setItem("currCity", currCity);
    findCitiesNearby(position);
}*/

function getCitiesFromServer() {
    $.ajax({
        type: 'get',
        url: 'dish',
        async: false,
        data: {
            'requestType': 'loadCities'
        },
        success: function (cities) {
            var citySelect = document.getElementById("selectCity");
            var myOption;
            myOption = document.createElement("option");
            myOption.text = "";
            citySelect.add(myOption);
            for (var i = 0; i < cities.length; i++) {
                myOption = document.createElement("option");
                myOption.text = cities[i].name;
                citySelect.add(myOption);
            }
        }
    });
    if (window.location.href == "http://localhost:8080/searchDish.html") {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(locationSuccess);
            searchNearby();
        }
    }
}

function locationSuccess(position) {

    var currCity = findCurrCity(position);
    var option = $('#selectCity option').filter(function() { return $(this).html() == currCity;});
    //  option.attr('selected', 'selected');
    //var citySelect = document.getElementById("selectCity");
    //citySelect[0].options[0].textContent = currCity;
    //TODO
}

function searchNearby() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(findCitiesNearby);
    }
    // TODO else

}

function findCitiesNearby(position) {
    var latitude = position.coords.latitude;
    var longitude = position.coords.longitude;
    var request = new XMLHttpRequest();
    var method = 'GET';
    var async = false;
    var urlApi;
    var restaurantsNearby = [];

    $.ajax({
        url: 'admin',
        async: false,
        data: {
            'requestType': 'getRestaurants'
        },
        success: function (restaurants) {
            for (var i = 0; i < restaurants.length; i++) { // find each city coords and check the distance
                urlApi = 'http://maps.googleapis.com/maps/api/geocode/json?address=' + restaurants[i].city + '&sensor=true';
                request.open(method, urlApi, async); // find rest coords
                request.onreadystatechange = function(){
                    if(request.readyState == 4 && request.status == 200) {
                        var data = JSON.parse(request.responseText);
                        var coords = data.results[0].geometry.location;
                        var lon = coords.lng;
                        var lat = coords.lat;
                        if (distance(latitude,longitude,lat,lon)<=10)
                            restaurantsNearby.push(restaurants[i]);
                    }
                };
                request.send();
            }
        }
    });

    sessionStorage.setItem("restNearby", JSON.stringify(restaurantsNearby));
}

function findCurrCity(position) {
    var latitude = position.coords.latitude;
    var longitude = position.coords.longitude;
    var request = new XMLHttpRequest();
    var method = 'GET';
    var url = 'http://maps.googleapis.com/maps/api/geocode/json?latlng=' + latitude + ',' + longitude + '&sensor=true';
    var async = false;
    var currCity;
    request.open(method, url, async);
    request.onreadystatechange = function () {
        if (request.readyState == 4 && request.status == 200) {
            var data = JSON.parse(request.responseText);
            var address = data.results[0];
            var formatted_address = address.formatted_address.replace(/\s*,\s*/g, ",");
            currCity = formatted_address.split(',').slice(1, 2);
        }
    };
    request.send();
    return currCity;
}

function distance($lat1, $lng1, $lat2, $lng2) {
    // convert latitude/longitude degrees for both coordinates
    // to radians: radian = degree * π / 180
    $lat1 = deg2rad($lat1);
    $lng1 = deg2rad($lng1);
    $lat2 = deg2rad($lat2);
    $lng2 = deg2rad($lng2);

    // calculate great-circle distance
    $distance = Math.acos(Math.sin($lat1) * Math.sin($lat2) + Math.cos($lat1) * Math.cos($lat2) * Math.cos($lng1 - $lng2));

    // distance in human-readable format:
    // earth's radius in km = ~6371
    return 6371 * $distance;
}

function deg2rad(degree) {
    return degree * Math.PI / 180;
}