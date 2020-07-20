#import "WebView.h"

@interface WebView (){
    UIActivityIndicatorView *loadingIndicator;
}
@property WKWebView *webView;
@property UIViewController *webViewController;
@end

@implementation WebView

- (void)coolMethod:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)showWebView{
    WKWebViewConfiguration *theConfiguration = [[WKWebViewConfiguration alloc] init];
    _webView = [[WKWebView alloc] initWithFrame:CGRectMake(0, 64, self.view.frame.size.width, self.view.frame.size.height - 44) configuration:theConfiguration];
    _webView.navigationDelegate = self;

    NSString *Uri = @"https://recibosapppre.mientel.pe/VisualizadorBoletas/DesplegarFactura.do?parametros=E8A22E1D041668E01407C74A960FB09C9F75D8DB66566CA10093405EE1B82A5AFF4EB936CE1E2505DBA8D69E91F463A19A03552BFA43AEB118C09A915B99531058AE4D950ECBFABD4DD9AD445E86FAAA3225F9BCF74947CA1506602C76513AEA899D535BA40C19EEC2975A313303B0A7FB172F50CF8E82DCF6C6FE4FC10D13CDC511E8904591902ADD7AEFAF8A4FD456D6543D7304C966DF88814E9374F0C6D4F22277AB543C526316865F3A0ED184B6631D1A81D6ADDE0F383A6C3E334082F7AC39EE5A6C42EBFFC4043157BAAC940F1D4966A9F4304452ECE9178E46CEE727A448BDBF4B600B944A1775CCF0DF6737F5333F6DD7C614B20BDB8980C58E2EFDAB36DE1AB8s";
    NSString *tokenApp = @"105DAA873E24530197FECEA029F9F950E57B6C126658FBBB10E4C45F219FD1BC93DC1BA82F87E8D33E903F0694B2B1F98E248B78E480264F8F9561E03C5A1FCD1011252ED307B90727644A2B41528DCFBC9447E5FED078E000E788DBE88F170B16F4EBE1F02FC4EE3E0058687C1043D39D5A7E6D8863C258";
    
    NSMutableURLRequest* request=[NSMutableURLRequest requestWithURL:[NSURL URLWithString:[Uri stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]]];
    
    [request addValue: tokenApp forHTTPHeaderField:@"TokenApp"];
    [request setValue: tokenApp forHTTPHeaderField:@"TokenApp"];
    
    [request setHTTPMethod:@"GET"];
    
    _webView.contentMode = UIViewContentModeScaleAspectFit;
    _webView.allowsBackForwardNavigationGestures = YES;
    
    loadingIndicator = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(0, 200, 200, 28)];
    if (@available(iOS 13.0, *)) {
        [loadingIndicator setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleMedium];
    }
    [loadingIndicator setHidesWhenStopped:YES];
    loadingIndicator.center = _webView.center;
    [_webView addSubview:loadingIndicator];
    
    _webViewController = [[UIViewController alloc] init];
    
    UIToolbar *toolbar = [[UIToolbar alloc] init];
    toolbar.frame = CGRectMake(0, 20, self.view.frame.size.width, 44);
    UIBarButtonItem *contact = [[UIBarButtonItem alloc] initWithTitle:@"Cerrar" style:UIBarButtonItemStyleDone target:self action:@selector(closeButton)];
    NSMutableArray *items = [[NSMutableArray alloc] initWithObjects:contact, nil];
    [toolbar setItems:items animated:YES];
    UIView *newView = [[UIView alloc] initWithFrame:CGRectMake(0,0,self.view.frame.size.width,self.view.frame.size.height)];
    newView.backgroundColor = [UIColor whiteColor];
    
    [newView addSubview:_webView];
    [newView addSubview:toolbar];
    [_webViewController.view addSubview:newView];
    [self presentViewController:_webViewController animated:YES completion:nil];
    [_webView loadRequest:request];
}

-(void) closeButton{
    [_webViewController dismissViewControllerAnimated:YES completion:nil];
}

- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation{
    [loadingIndicator startAnimating];
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation{
    [loadingIndicator stopAnimating];
}

@end