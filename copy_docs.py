import shutil
import os

def copy_tree(src, dst, symlinks=False, ignore=None):
    for item in os.listdir(src):
        s = os.path.join(src, item)
        d = os.path.join(dst, item)
        if os.path.isdir(s):
            if not os.path.exists(d):
                os.mkdir(d)
            copy_tree(s, d, symlinks, ignore)
        else:
            shutil.copy2(s, d)

fromDir = "run/config/craftbook/documentation/"
toDir = "../CraftBookDocs/source/"

copy_tree(fromDir, toDir)