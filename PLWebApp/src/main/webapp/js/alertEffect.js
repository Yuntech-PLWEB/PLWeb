$(function() {
        $(document).ready(function() {
                function fadeInOut(){
                        $('.needHelp').fadeOut(500, function(){
                                $('.needHelp').fadeIn(500, function(){
                                        setTimeout(fadeInOut, 300);
                                });
                        });
                }
                fadeInOut();
        });

});

