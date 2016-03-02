$(document).ready(function(){
    $("#step2").hide();

    $(document).ready(function(){
    $('.mainHeading').slideDown(1000);
});
    
    
    //Click handler
    $("#go").on("click", function(){
        
        $("#step2").slideDown(1000);
        $(this).fadeOut("slow");
    });//End Click
    
  
    
    
    
});//End Ready

  