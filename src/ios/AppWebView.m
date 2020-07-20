#import "AppWebView.h"
#import "AppViewController.h"


@implementation AppWebView

- (void)coolMethod:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSArray* arguments = [command.arguments objectAtIndex:0];
    NSString * url = [arguments objectAtIndex:0];
    NSMutableString * strHeaders = [arguments objectAtIndex:1];
    NSData *data = [strHeaders dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSDictionary *jsonHeader = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
    NSDictionary * headers = [[NSDictionary alloc] initWithDictionary:jsonHeader copyItems:YES];
    AppViewController * apv = [AppViewController sharedHelper];
    apv.url = url;
    apv.headers = headers;
    [apv showWebView];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];


    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
