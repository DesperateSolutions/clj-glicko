var gulp = require('gulp');
var browserify = require('browserify');
var reactify = require('reactify');
var watchify = require('watchify');
var source = require('vinyl-source-stream');
var notify = require("gulp-notify");

function buildScript(file, watch) {
    var bundler = watchify(browserify({
        entries: [file],
        transform: [reactify],
        debug: true,
        fullPaths: true
    }));

    function rebundle() {
        var stream = bundler.bundle();
        return stream.on('error', notify.onError({
            title: "Compile Error",
            message: "<%= error.message %>"
        }))
        .pipe(source('app.js'))
        .pipe(gulp.dest('./resources/public/build/'))
        .pipe(notify("Rebundled.."));
    }

    bundler.on('update', function () {
        rebundle();
        console.log("Rebundling..");
    });

    return rebundle();
}

gulp.task('build', function() {
    return buildScript('./resources/public/js/app.js', false);
});

gulp.task('default', ['build'], function() {
    return buildScript('main.js', true);
});
