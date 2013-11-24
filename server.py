

import tornado.httpserver
import tornado.websocket
import tornado.ioloop
import tornado.web
import bluetooth
import json
import os

import datastore_example as ds

class MainHandler(tornado.web.RequestHandler):
    def get(self):
        self.render('index.html')

class WSHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        print 'new connection'
        self.write_message("Started!")

    def on_message(self, message):
        nearby_devices = bluetooth.discover_devices(duration=2, lookup_names=True)

        msg = "found %d devices" % len(nearby_devices)
        for addr, name in nearby_devices:
            msg += "%s" % (addr,)

        self.write_message(tornado.escape.to_basestring(msg))
        print msg

    def on_close(self):
        print 'connection closed'


class WSHandlerMap(tornado.websocket.WebSocketHandler):
    def open(self):
        print 'new connection2'
        self.write_message("Started!")

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
])

if __name__ == "__main__":
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()
