process.setPaths = function () {
    process.cwd = function () {
        return HOME_PATH + '/' + ASSETS_PATH;
    };

    process.userPath = HOME_PATH;
};

process.registerAssets = function (from) {
    var fs = from;
    if (!fs || !fs.existsSync)
        fs = require('fs');

    var path = require('path');
    var folders = process.natives.assetReadDirSync();
    var root = process.cwd();

    // patch execPath to APK folder
    process.execPath = root;

    function createRealPath(pd) {
        var arr = [pd, pd + "/www", pd + "/www/jxcore"];

        for (var i = 0; i < 3; i++) {
            try {
                if (!fs.existsSync(arr[i]))
                    fs.mkdirSync(arr[i]);
            } catch (e) {
                console.error("Permission issues ? ", arr[i], e)
            }
        }
    }

    createRealPath(process.userPath);

    var sroot = root;
    var hasRootLink = false;
    if (root.indexOf('/data/user/') === 0) {
        var pd = process.userPath
            .replace(/\/data\/user\/[0-9]+\//, "/data/data/");
        createRealPath(pd);
        sroot = root.replace(/\/data\/user\/[0-9]+\//, "/data/data/");
        hasRootLink = true;
    }

    var jxcore_root;

    var prepVirtualDirs = function () {
        var _ = {};
        for (var o in folders) {
            var sub = o.split('/');
            var last = _;
            for (var i in sub) {
                var loc = sub[i];
                if (!last.hasOwnProperty(loc))
                    last[loc] = {};
                last = last[loc];
            }
            last['!s'] = folders[o];
        }

        folders = {};
        var sp = sroot.split('/');
        if (sp[0] === '')
            sp.shift();
        jxcore_root = folders;
        for (var o in sp) {
            if (sp[o] === 'jxcore')
                continue;

            jxcore_root[sp[o]] = {};
            jxcore_root = jxcore_root[sp[o]];
        }

        jxcore_root['jxcore'] = _; // assets/www/jxcore -> /
        jxcore_root = _;
    };

    prepVirtualDirs();

    var findIn = function (what, where) {
        var last = where;
        for (var o in what) {
            var subject = what[o];
            if (!last[subject])
                return;

            last = last[subject];
        }

        return last;
    };

    var getLast = function (pathname) {
        while (pathname[0] == '/')
            pathname = pathname.substr(1);

        while (pathname[pathname.length - 1] == '/')
            pathname = pathname.substr(0, pathname.length - 1);

        var dirs = pathname.split('/');

        var res = findIn(dirs, jxcore_root);
        if (!res)
            res = findIn(dirs, folders);
        return res;
    };

    var stat_archive = {};
    var existssync = function (pathname) {
        var n = pathname.indexOf(root);
        if (hasRootLink && n == -1)
            n = pathname.indexOf(sroot);
        if (n === 0 || n === -1) {
            if (n === 0) {
                pathname = pathname.replace(root, '');
                if (hasRootLink)
                    pathname = pathname.replace(sroot, '');
            }

            var last;
            if (pathname !== '') {
                last = getLast(pathname);
                if (!last)
                    return false;
            } else {
                last = jxcore_root;
            }

            var result;
            // cache result and send the same again
            // to keep same ino number for each file
            // a node module may use caching for dev:ino
            // combinations
            if (stat_archive.hasOwnProperty(pathname))
                return stat_archive[pathname];

            if (typeof last['!s'] === 'undefined') {
                result = { // mark as a folder
                    size: 340,
                    mode: 16877,
                    ino: fs.virtualFiles.getNewIno()
                };
            } else {
                result = {
                    size: last['!s'],
                    mode: 33188,
                    ino: fs.virtualFiles.getNewIno()
                };
            }

            stat_archive[pathname] = result;
            return result;
        }
    };

    var readfilesync = function (pathname) {
        if (!existssync(pathname))
            throw new Error(pathname + " does not exist");

        var rt = root;
        var n = pathname.indexOf(rt);

        if (n != 0 && hasRootLink) {
            n = pathname.indexOf(sroot);
            rt = sroot;
        }

        if (n === 0) {
            pathname = pathname.replace(rt, "");
            pathname = path.join('www/jxcore/', pathname);
            return process.natives.assetReadSync(pathname);
        }
    };

    var readdirsync = function (pathname) {
        var rt = pathname.indexOf('/data/') === 0 ? (hasRootLink ? sroot : root)
            : root;
        var n = pathname.indexOf(rt);
        if (n === 0 || n === -1) {
            var last = getLast(pathname);
            if (!last || typeof last['!s'] !== 'undefined')
                return null;

            var arr = [];
            for (var o in last) {
                var item = last[o];
                if (item && o != '!s')
                    arr.push(o);
            }
            return arr;
        }

        return null;
    };

    var extension = {
        readFileSync: readfilesync,
        readDirSync: readdirsync,
        existsSync: existssync
    };

    fs.setExtension("jxcore-java", extension);
    var node_module = require('module');

    node_module.addGlobalPath(process.execPath);
    node_module.addGlobalPath(process.userPath);
};

// if a submodule monkey patches 'fs' module, make sure APK support comes with it
var extendFS = function () {
    process.binding('natives').fs += "(" + process.registerAssets.toString() + ")(exports);";
};

process.setPaths();
process.registerAssets();
extendFS();

jxcore.tasks.register(process.registerAssets);
jxcore.tasks.register(process.setPaths);
jxcore.tasks.register(extendFS);

if (typeof MAIN_FILE === 'string') {
    require(MAIN_FILE);
} else {
    console.error('MAIN_FILE is not a string');
}
