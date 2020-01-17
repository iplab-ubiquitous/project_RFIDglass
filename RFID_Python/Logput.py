import datetime


class Logput:
    def __init__(self, type):
        self.path = "log/" + type + "Log"
        self.f = open(self.path, mode='a')
        self.f.write(datetime.datetime.today().strftime("%Y-%m-%d  %H:%M:%S   \n"))

    def logput(self, str):
        self.f.write(str)

    def __del__(self):
        self.f.write("\n\n")
        self.f.close()