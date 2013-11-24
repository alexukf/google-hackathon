

import tornado.httpserver
import tornado.websocket
import tornado.ioloop
import tornado.web
import bluetooth
import json
import os
import subprocess

import datastore_example as ds


known_devices = {'a1':'Cristi', 'a2':'Alex'}

def get_temperature():
    p = subprocess.Popen(['thermometer'], stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, shell=True)
    return out.strip()

class MainHandler(tornado.web.RequestHandler):
    def get(self):
        self.render('index.html')

class WSHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        print 'new connection'

    def on_message(self, message):
        nearby_devices = bluetooth.discover_devices(duration=2, lookup_names=True)

        msg = []
        for addr, name in nearby_devices:
            msg.append(addr)

        #self.write_message(tornado.escape.to_basestring(msg))
        self.write_message(json.dumps(msg))
        print msg

    def on_close(self):
        print 'connection closed'

class WSHandlerTemp(tornado.websocket.WebSocketHandler):
    def open(self):
        print 'new connection temp'
        self.write_message("Started!")

    def on_message(self, message):
        data = json.dumps(['temp', get_temperature()])

        self.write_message(data)
        print data

    def on_close(self):
        print 'connection closed temp'

class WSHandlerMap(tornado.websocket.WebSocketHandler):
    def open(self):
        print 'new connection2'

    def on_message(self, message):
        data = ds.get_data('stone-notch-404')

        self.write_message(data)
        print data

    def on_close(self):
        print 'connection closed2'


application = tornado.web.Application([
    (r'/', MainHandler),
    (r'/ws', WSHandler),
    (r'/ws2', WSHandlerMap),
    (r'/ws3', WSHandlerTemp),
    (r'/img/(.*)',tornado.web.StaticFileHandler, {"path": "./img"},),
    (r'/js/(.*)',tornado.web.StaticFileHandler, {"path": "./js"},),
    (r'/css/(.*)',tornado.web.StaticFileHandler, {"path": "./css"},),
    (r'/font/(.*)',tornado.web.StaticFileHandler, {"path": "./font"},),
    (r'/fancybox/(.*)',tornado.web.StaticFileHandler, {"path": "./fancybox"},),
])

if __name__ == "__main__":
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()
