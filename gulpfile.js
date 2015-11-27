var gulp = require('gulp');
var browserify = require('browserify');
var reactify = require('reactify');
var watchify = require('watchify');
var source = require('vinyl-source-stream');

gulp.task('browserify', function() {
    var bundler = browserify({
        entries: ['./resources/public/js/app.js'],
        transform: [reactify],
        debug: true,
        fullPaths: true // Requirement of watchify
    });

    var watcher = watchify(bundler);

    return watcher
        .on('update', function() {
            console.log("Updating..");
            watcher.bundle()
            .pipe(source('app.js'))
            .pipe(gulp.dest('./resources/public/build/'));
            console.log("Done..");
        })
        .bundle()
        .pipe(source('app.js'))
        .pipe(gulp.dest('./resources/public/build/'));
        console.log("Done..");


});

gulp.task('default', ['browserify']);