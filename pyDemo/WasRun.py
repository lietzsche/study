from TestCase import TestCase


class WasRun(TestCase):
    def __init__(self, name):
        TestCase.__init__(self, name)
    def testMethod(self):
        self.log += "testMethod "
    def setUp(self):
        self.log = "setUp "
    def tearDown(self):
        self.log += "tearDown "