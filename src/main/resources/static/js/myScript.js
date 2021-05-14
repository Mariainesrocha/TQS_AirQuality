GET: $(document).ready(
function () {
        // GET REQUEST 1
        $("#searchButton").click(function (event) {
            event.preventDefault();
            var value = document.getElementById('searchInput').value;

            if (! $.isNumeric(value)){
                $('#spinnerModal').show();
                ajaxGet("places/"+ value);
            }
            else {
                $('.modal-body').html("Error with search value must be a string");
                $("#myModal").modal('show');
            }
        });

        // GET REQUEST 2
        $("#searchButton2").click(function (event) {
            event.preventDefault();
            var lat = document.getElementById('lat').value;
            var lng = document.getElementById('lng').value;

            if ($.isNumeric(lat) && $.isNumeric(lng)){
                $('#spinnerModal').show();
                ajaxGet("places?lat="+ lat+"&long="+lng);
            }
            else {
                $('.modal-body').html("Error with latitude and longitude values");
                $("#myModal").modal('show');
            }
        });

        // DO GET
        function ajaxGet(url) {
            $.ajax({
                type: "GET",
                dataType: 'json',
                url: url,
                success: function (place) {
                    if($('#searchTable tr').length > 2)
                        $("#searchTable").find("tr:gt(0)").remove();
                    var trHTML = '';
                    if ($("#searchTable tr:contains("+place.name+")").text() == "") {
                        trHTML += '<tr style="height: 75px;" >' +
                            '<td align="center">' + place.name + '<div>'+ (place.latitude).toFixed(3) + " ; "+ (place.longitude).toFixed(3) + '</div></td>' +
                            '<td align="center">' +
                            '<div> <label style="font-weight: bold"> Air Quality Index: </label> <span>' + place.airQualityInfo[0]+ '</span></div>'+
                            '<div><label style="font-weight: bold"> Pollutant: </label> <span>' + place.airQualityInfo[1]+ '</span></div>' +
                            '<div><label style="font-weight: bold"> Concentration: </label> <span>' + place.airQualityInfo[2]+ '</span></div>';

                        var newCol2 = '<td><div><label style="font-weight: bold"> --------------------- </label></div></td>';
                        if (place.airQualityInfo.length > 3){
                            trHTML += '<div><label style="font-weight: bold"> Category: </label> <span>' + place.airQualityInfo[3]+ '</span></div>';
                            newCol2 = iteratePollenMaps(place.pollen_count,place.pollen_risk);
                        }

                        var newCol = iterateMap(place.airElements);
                        trHTML +='</td>' + newCol  + newCol2 + '<td align="center">' + moment(place.updateDate).format('D-MM-YYYY\nh:mm:ss A') + '</td>' +
                        '<td><i type="button" onclick="deleteRow(this)" class="fa fa-times-circle" style="color: red"></i></td></tr>';
                    }
                    $('#searchTable tbody').append(trHTML);
                    ShowTable();
                },
                error: function (e) {
                    $('#spinnerModal').hide();
                    $('.modal-body').html(" Failed to search, try again later! \n If the error persists maybe we don't have information about the chosen city.");
                    $("#myModal").modal('show');
                    console.log(e);
                }
            });
        }
    })

function ShowTable(){
    var table = document.getElementById('searchTable');
    table.style.display = "block";
    $('#spinnerModal').hide();
}

function iterateMap(data){
    var newCol = '<td align="center">';
    $.map(data, function(val, key) {
        newCol += '<div><span style="font-weight: bold">' + key + '</span> : <span>' + val.toFixed(2) + '</span> </div>';
    });
    newCol += "</td>";
    return newCol;
}


function iteratePollenMaps(data1,data2){
    var newCol = '<td align="center">'
    $.map(data1, function(val, key) {
        newCol += '<div><span style="font-weight: bold">' + key + '</span> : <span>' + val + '</span> - <span>' + data2[key] + '</span> </div>';
    });
    newCol += "</td>";
    return newCol;
}

function deleteRow(btn){
    var i = btn.parentNode.parentNode.rowIndex;
    var table = document.getElementById('searchTable');
    table.deleteRow(i);
    if ($('#searchTable tr').length == 1)
        table.style.display = "none";
}