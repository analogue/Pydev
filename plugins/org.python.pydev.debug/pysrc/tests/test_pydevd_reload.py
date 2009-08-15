'''
Changed the doctest frome the xreload to actual unittest.
'''

import sys
import os
sys.path.append(os.path.split(os.path.split(__file__)[0])[0])

SAMPLE_CODE = """
class C:
    def foo(self):
        return 42
    @classmethod
    def bar(cls):
        return 42, 42
    @staticmethod
    def stomp():
        return 42, 42, 42
"""

import shutil
from pydevd_reload import xreload
import tempfile

tempdir = None
save_path = None
import unittest

class TestCase(unittest.TestCase):
    

    def setUp(self, nused=None):
        global tempdir, save_path
        tempdir = tempfile.mkdtemp()
        save_path = list(sys.path)
        sys.path.append(tempdir)
    
    
    def tearDown(self, unused=None):
        global tempdir, save_path
        if save_path is not None:
            sys.path = save_path
            save_path = None
        if tempdir is not None:
            shutil.rmtree(tempdir)
            tempdir = None
            
    
    def make_mod(self, name="x", repl=None, subst=None):
        assert tempdir
        fn = os.path.join(tempdir, name + ".py")
        f = open(fn, "w")
        sample = SAMPLE_CODE
        if repl is not None and subst is not None:
            sample = sample.replace(repl, subst)
        try:
            f.write(sample)
        finally:
            f.close()

        
    def testMet1(self):
        self.make_mod()
        import x #@UnresolvedImport -- this is the module we created at runtime.
        C = x.C
        Cfoo = C.foo
        Cbar = C.bar
        Cstomp = C.stomp
        b = C()
        bfoo = b.foo
        self.assertEqual(b.foo(), 42)
        self.assertEqual(bfoo(), 42)
        self.assertEqual(Cfoo(b), 42)
        self.assertEqual(Cbar(), (42, 42))
        self.assertEqual(Cstomp(), (42, 42, 42))
        self.make_mod(repl="42", subst="24")
        xreload(x)
        self.assertEqual(b.foo(), 24)
        self.assertEqual(bfoo(), 24)
        self.assertEqual(Cfoo(b), 24)
        self.assertEqual(Cbar(), (24, 24))
        self.assertEqual(Cstomp(), (24, 24, 24))
        
        
#=======================================================================================================================
# main
#=======================================================================================================================
if __name__ == '__main__':
    unittest.main()