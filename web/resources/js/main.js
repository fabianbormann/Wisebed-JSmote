function manageExperimentLogin(experimentLogin){
    if(experimentLogin)
        showExperimentLogin();
    else
        greyOutExperimentLogin();
}

function greyOutExperimentLogin(){
    $("#loginForm\\:nodeUrns").attr("readonly", "true");
    $("#duration").attr("readonly", "true");
    $("#offset").attr("readonly", "true");
}

function showExperimentLogin(){
    $("#loginForm\\:nodeUrns").removeAttr("readonly");
    $("#duration").removeAttr("readonly");
    $("#offset").removeAttr("readonly");
}

function okese(){
    alert("hi");
}