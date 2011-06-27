import authlib
import sys

DEV_ID = "njfdsfdsYQWVZgKMypgalUGXYZjWa2ddhZCiocsqKe2PtXxBoM3y1SsSxrZbEy4UQ3nOnZs7UlkNMZ5DkrW5CHMBTtndvtHWN_xB4osm3hxJHevPWHBoO48KXZ7bu8HowxXG2aY8tp3rheOAJEladhFwQjs8wiGwmqLhwbDfsRfsZZoTSrOqErocTeOvseH7nxOXCByQ_WN0OkQDtkTg6Obz5plAXMxoimrK4UwO"
afdConn = authlib.AppEngineClient("androidfiledrop", "prestomation@gmail.com", sys.argv[1], True)
print afdConn.registerDevice(DEV_ID, "Evo").read()
#print afdConn.getDevices().read()


