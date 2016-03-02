function MyViewModel() {
    var self = this;
 
    self.movies = ko.observableArray();
    self.searchTerm = ko.observable();
    self.moreInfo = ko.observableArray();
    self.currentMovie = {
        Type: ko.observable(),
        Year: ko.observable(),
        Genre: ko.observable(),
        Released: ko.observable(),
        Runtime: ko.observable(),
        Poster: ko.observable(),
        Rated: ko.observable(),
        imdbRating: ko.observable(),
        imdbVotes: ko.observable(),
        Actors: ko.observable(),
        Plot: ko.observable(),
        Writer: ko.observable(),
        Director: ko.observable(),
        Country: ko.observable(),
        Language: ko.observable(),
        Title: ko.observable()
        
    };

    self.error = ko.observable(false);
    self.errorMessage = ko.observable();

    self.searchForMovies = function () {
         if($(".searchForm").val() == ""){
             alert("Plese Enter a search movie!")
             return;
         }
        
        $.getJSON("http://omdbapi.com/?s=" + self.searchTerm(), {
            api_key: '7a5035cca9022ea67734112eb921d5d33680a63d'
        }).done(function (data) {
            self.error(false);
            self.errorMessage('');

            if (data.Search) {
                ko.mapping.fromJS(data.Search, {}, self.movies);
            } else {
                self.error(true);
                self.errorMessage(data.Error);
            }
        });
    };

    self.viewMoreInfo = function (movie) {
        $.getJSON("http://omdbapi.com/?t=" + movie.Title(), {
            api_key: '7a5035cca9022ea67734112eb921d5d33680a63d'
        }).done(function (data) {
            self.error(false);
            self.errorMessage('');

            if (data) {
                ko.mapping.fromJS(data, {}, self.currentMovie);
            } else {
                self.error(true);
                self.errorMessage(data.Error);
            }
        });
    };
    
    self.clear = function(){
        $(".searchForm").val("");
        $(".results").fadeOut("fast");
    };
};



ko.applyBindings(new MyViewModel());