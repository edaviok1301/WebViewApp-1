#import "AppWebView.h"
#import "AppViewController.h"


@implementation AppWebView

- (void)coolMethod:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSArray* arguments = [command.arguments objectAtIndex:0];
    NSString * url = [arguments objectAtIndex:0];
    NSDictionary * headers = [arguments objectAtIndex:1];
    
    [[AppViewController sharedHelper] showWebView];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
