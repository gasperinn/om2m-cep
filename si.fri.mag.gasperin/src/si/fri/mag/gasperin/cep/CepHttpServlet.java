package si.fri.mag.gasperin.cep;

import java.util.List;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; 

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;

import si.fri.mag.gasperin.cep.h2.CepRule;
import si.fri.mag.gasperin.cep.h2.Device;
import si.fri.mag.gasperin.cep.h2.H2DBTableCepRules;
import si.fri.mag.gasperin.cep.h2.H2DBTableDevices;
import si.fri.mag.gasperin.cep.utils.CEP;
import si.fri.mag.gasperin.cep.utils.DataInterface;
import si.fri.mag.gasperin.cep.utils.RequestSender;


public class CepHttpServlet extends Thread{
	
	private static Server server;
	private CEP cep;
	private static boolean debug = false;
	
	//use same credentials as for webpage
	private String userName = null;
	private String pass = null;
	
	private String cepLogoImage = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASgAAABaCAYAAAALmstnAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAB3RJTUUH4AIaBxIGCyyW2gAAT9FJREFUeNrtvXeQpOl93/d53tQ5TE9Pnp3ZnG739nLCAThEQSApZsqiaFmWypIsS1VyqSw5/GG5XCqXXSVXSZZctmRTpk1BIimBBDPCAXcADne4fLc5z+7s5OnpHN5+w+M/3u6eztM94XAA97s1u7Pdb3ji7/nln5BSSj4idH2TAPFRNeAhHuIhfqygHfQLJHA/U+bKRoFC1UG2fakIGAnonJ+IMB4yftTj8RAP8RAfI4iD5qBub5X46pU1sqbV97rpiJ9fOTdJMviQSO0EV0qqtkPFsjEtB8d1AY8T1VQFn67h1zV0VUGIh/zpQUICpmWTK5nkyiaFSpVS1aJqbx/GmiLwaRoBn07YbxAL+Aj7DQxN/VE3/2OPA+WgJPDBap6MaaHusFGW8ybXNoq8OP+QQLVDAsVKlaV0nrvraRZTOVL5EoVKFdN2cGsECiG8zaBrRAI+xiJBDo/FOTwWZzIexq/vbbpNyyZdqsAQR5oiBIoiUIRAVRR0TUFTFBRFoAoxMAF1pcS0HOovlxJky+80dAiydn39P66UjSZLKZFN12mKQjzkRxmCkLuuJFUoc3cjza3VLW8+CiXKVQvLcXFd2arOEJ6koAgFQ1MI+QxGI0EOJaIcn0xwZDxOPBhgmLOkajuki2WGYS9EbS7U+lyoCpqq1OZm8LmQtblwkYi2uUCCO+BcIGXjOwmoikI86EdVtttxoByUIyVf+XCF65uFHReAKyUvzif40vHkQTXnxw6243J/M8t7CytcWdpkPVugYtm4EkRDd9c+rs2bEVRFEPTpzIxEeHRuggvzk0zEQrvirF67schX37za9a29IIRobABVUfDpKn5dw9DUBpf3xJEpLsxP9n3OwkaG3379MpbjgvA2ietuL13Hla0Eq2lZuw2iJJA0bQoJ8aCPv/n5p0hGgjv2pWo73Fzd4q3bS1xf3iRdrGDX2iNqfd0JdUIqa//RNYVkJMRjhyd57vgs0yPhgZ7z7t0V/u0PLnprYci5UJTtg8ynaY050VSF84fGefrYTN/nLKfzfOW1i5iWDUJ0zkXzITDgXCAh6NP5G597kumRSOP6A9dBPcTwcFzJ7bUtXr26wOUHGxQqVcDjRoQQqH1XpNhesLVfiqbF9ZUUN1a2+M6VBZ4+Os0nz8wzHg0N1a6yaZEtVZrfMCBk09/bH3mnq4uqKDsSqHubWW6spgZ+4yAtlIBlO5Sr9o7XLmxk+PoHt7i0uE65ansHRG2zDwMBIGojKLy5XsnkWXk/z5u3HvDi6XleOjNPJODr+5yyZZMpmUNxs9u9bvutaS4s2+HJo9N9GYqlrRw3VlLbxGWQPg+AUtWi1KYKOlgC1UY9H2JnbBXKfPPibV6/+YBCuYqoiUd7QfPpnsqX+NMPbvHB/TW+/NgJnj42jaooAz1HUxUEYihRZLsFbQu19h8hFTbzJSzbQe+jk9nIFT2jypAEYSfUN2YvuFLyw1tL/N5b10jlS9587HMb6vO7VSjzB+9c59Zqil9+9hFmR6M979EUBUXsgj41zUL7QSakQqpQxrRsAobe8wkb+RKulHtel+2QeNxXSz/39Q1dXuh+zOjTzlRfsM/rb2DcWEnx7394hbvraa8dB9CQOqFaTuf5ze9/yEa+xJcePdaXONShiP33CRF4G7NoWsR7tMGVklS+tIvNuPO7pZQ4PRapBF67vsjvvHGZUtU6kPloaU9tbi4/2CBfeY+/+qnHmEvGul7rEQfBbkhUv/HIlCrkK9W+BGozV6Impe0rvLloPSwOXMTbbyOShBZ5t/6Onah5vmrzzlKO5Xyl75SqQnBkJMDjU1EMdTDOYj/69O6dZX77jcukCuV9P5m6QREC03L44/duoCqCL104vvN7D6JZQpCvmKSLFeIhf9dLKlWbzXxp39cS9CdQ15Y2+L23rlKuWh/JnNShCMH9zSy/9fol/rPPPUk82DkuB9EcIQTFikUqX+4p/ldth/Vc8YDmgo65OFACJdiZcAyDvGnzg8UM68Vqy+eKgJOjIZ6YirZYAOpwXMnLt1O8vZwd6D1XNwtUHckn50cOcngaePfOMv/mtYvkyuZHuhGEANuRfP2DW0zGwjxxZKrv9aqi7DuNEkDFcljPFTgyHu96TbZcIV3cje5rZ7iSjlMboGhW+aP3bpL9iOekDkUIbqxs8eqVBX72qdMd36uK2Gf+yUPVcVjLFjgz091Yla9USRXKB3JWSSSO8xFyUC5gu+7A14f0/mLGeys5vndvq0vHYCFTZjLs41Cs87TJmjY3t0oD608cV3J1o8AzszF8B8xF3VhJ8dtvXPnIiVMdQnhK9N97+xpTIxGm4uHe1x5QGxzXZSVd6Pn9Rq5E0bQO5tRGdnDkAB/eX+fW6taPZE6aW/f6zQc8e3yWyY55OZh2SVeynM73/D6VL5EvmwfjX9flsDjQ3ZetWKTLNgKBW/OB6PUzFfFzeqy/VSlT8awtSt1cWjdhC4HluOR7WGMsp8bGDzymAtvtvnD3E1uFMv/hzaukCqU9bQTJ3k5SRQiW03n+5P2bWI5zoH3u2n4JK5l8T1FrJZ3HPqB2Sdl5iNquy3sLK54Lwa6e6a03x5UDW7q6QQhBqlDm0uL6gfS9+0thNVOgancf737f7RXdxO194aCqjstaoUrF3vZortgO76/myVQsNFUwFfYR82toXUSwiKFxYTLK2E5e5IOa1/cBHc9aXoDvfBUK2aZvJURG4LO/AJNzQz3flZJvXrzDnbXdn9KulOiqUnPAFJiWTdVxdvU8RQjeubPCo3MTPHV0eh9HcmcIAeu5IqWqRcTfugakhKV0Hleyg3vF7iDp3BRbhTL3NrK74tikhLFoiJNTowQMndVMnpurW5i2s6v16bqSG6spPvPIka7qi/2GADZrTsCJcKDj++V0HteVB2IwkHQeFnsmUI6UfOfuFm8tZVtMhK6UuK4nK790OMGzs3FPXOrSr499MIZZhn/6X8E3/n337y/9EP7RvwbDP/Aj766neePmg1313vOAFjw25zn3jUdDICCVL/P2nWXeXVihajlDbzDTtvn25bucmUkS8n10Hv1CCDJFk0yx3EGgKpbNWvZglLJQV8y2borVTIHcLsQYKSWzozH+009vW98sx+GVKwv87lvXdsWRCeG5WJSrFmH/wc+JEKKhZ2onUJbjsJIp7PLJg6GvFc8Tt/o/wAtP2P5/zrS5tJ6nYrtdzfNxv8ZjU1H82sFbxA5MIMul4cYHNa1/23cu3nfF/MAESkrJGzcfkK/sTu/k01S+/PgJPvfIEXxN4SuziSiPzI4xn4zxu29fwxqSFVeE4O56mkuL6zx7fPYjG18BlKsWa9kih0Zbzeq5coWtQvlAFORepzpN2xu5Erbj7ELPInjh5GyLa4Cuqrx4ao737q5yYzU19Hx7Y2NjWnYbgTo49YNp2axm8pyYTLR8XqhUD8yaCnVxu4uI50jJpbUCVzYKmHZ/Kh/3azwzG2M64m3GuqzdrdESSVBX8e+TolkToqv/RXNQ5gENHX2DnkRzK3bGZqHM5Qcbu2P5peSpo9N88fwxtC7jqqkKL509zIOtHN+/fn/oDWHZLm/fWeaJI9Pobc93XJcDcH8BPNZ+aSvPU0dbP/cU5NWD2xR0mrYzpfLwjohSEjB0jox3Wn79hk4yGuT6SmpXg9eINWyCF9pzMHBdydJWp6I8VSgfnILc62l3Dur6ZpHfv7bW0CH1f4RkJW/yly9ME/Vp2wPYZQM3Ptqn/pweC3EzVeyqDJ+PB5iO9AoPqMX/yAEntTn4sY59nJM7a1ueqXYXEx3QNZ49PtuVONWhqQrPnZjlnbsrXrzUEBBCcHc9w0au2BITVR+XA4OE5XQOx5UtupaVTAHL3g03MzjaN4WUEPIZiFqc2SBwpeTI+Agz7WNWe37RtHa9hAxNRVdbLdwHGqAhPF1T1XZaMi6sZbxY0INCt8NCA7iZKlK2nR0zDtRbv1aosFowGwSqwWF03F8nDPvTgSMjQf7KYzMUq62ii6jllAru4KYgkQOJCv3JWGcfh8XN1S0cxx1a0ehKyXgszKE+IRB1HBqNMRkLcXcjMxQnIATkKyb3NrOdBOogIWA926koXz5ABTl03xSfP3+UZ47NNAW2DvacaMDX1QM7U6ywks7vTukORPwGfv2jS83SS1G+dIAK8jrcdg5K4kXND0OSXTwHvwaaw+fbOlqyHCq2i28fdFACjxCNBPSh7qs6rndK1kPId4Js64tQaj900qd61OeA56Np2Z6fyS5Z/bnRKKEBlKVBn870SJQ765mh3+U4ksXNLM+fmB3uxj1ACEGmVCFd2FaUVyybtUzhwMQ7oKaDal0T8aC/q/f2bvHBvTUvjm8XHZESJmKhFl3jQUMIQb5sksqXGgTKdlxWD1hBDp2HRQvVkAP+qV8NEDJURoM6juviSNnyI6UkXa7ywWoW03YbPk/OAMr4/cRCukzFdgbuoSslcb++HeoSTcD55+hqBVAUePQ5CMcGaku+Uq0pfYeHIgSzo7GB7hXAeCy06829lit29bA+KNSVwWvZ4vZYlc0D81puxkH2cy1b4NWrC7v2h1IVwdGJxK7u3QtMu9ViVzCrbOQOTkFeR3c3gwbHMOjbtwc7oKl8+eQ4769kMZvMqFVHspqvsFGs8u3bm1xdLxAyttlUVRGcHA1xYSq2J+W2lJ4lsdyUzKwxyI7kbrrEG/e3GnoytZYdoN8bp2M+XpxPbOtCDB/83f8ZnvuCZ62rz5KUHmF65nOgD2YCzpQqNa/o4fusqQrj0Z3zFtUxbCK2BgTkShVM2yFo7J3zdaVEiJ2Fa9t1WU7nAM8Pqy5mHKT+qZuIt18omRZfe/t6TbzbDfckGQ0HOTm1fwTKlXKgiArXlSw1eZSnC2Vy5crBBAE2OtwZZ6t5nzdSfQ34nNZGTkf9TEf9bddAqlTl3324xHKuwr1MqfV7CTc2CoQNjVNjTW78+TS88yqkNzoJwROfhtGJlsF+czHD64tblKxOk7rjSo9oSvBpChem4hxNBAloas9xVoRgNGg06ddqSE7Cn/+1Pc9BplgZ2vxfH09DU3fME9SMkE9HVcTQG1Dg5Ruq2g5BYzhxuh2aonBqepTFVI5syey7vqWE5XShoShfyRSoOrtzcBwGB8FBlas2X3v7Gu/cWd41gZXAo/MTQ+ft6gVFEZyZSrKWLZLKl3ekNauZfCMNzmq2SMWyD3wuejtqStlBeHpBDEDIBJAMGhyK+lnKdkboC+F5m68WKtsEynXg//7H8Fv/HBy7lVoLAV/8j+C/+z/B58nFS9kK37q9QbFqd10EDX9vKXl8OsZPnZr4EcdWeRyU47rDL1oJei3f+KDwaSqKEDgDHjzNsB23I3BzWEgJAUPjF589yzc+vM1r1xf7GmLqHuV1p8S61/IgHtR7yU+03wSqUKny1beu8tr1xV3bh6SUJCNBPnV6fl84yPoB93NPneaNW0u8fPFO3+cK4XGwebNKQguwnPZCkQYZ40E55m5t7BGLJ4f8GRyGpvR9TotoXszDe98D2/J0O17aQu/HdeGD1zzOqobVQoWSZaMIj2i2/9TfoymCE6OhvoNbrDqs5Css57Z/1gom1T1u0nbkSuYuTcSykUd6UGh7KJogB3XJGKDNfl1jLhnbMc+W51HuZS6wakrZnZov8VwCnjk2Q8DQd5Vg0t5HEW8zX+L/+96HfP/a/T0la1RVhS8+eqxv0rqhnym8NL/zydiOljghBLlyla18GSklK+n8jltfAn5d45ljM0T8xq7Wj+N0EfF28kNsbYSsqawGvqH3s9u/kM3uCm0DWF+pTRTWdj1FfD9aLfHMooE+Ztq1gsnXrqywVqi0tFsIwclkmJ8+M7mjC8OgKJjVXU2cBHy6OlQlkP32vm4YQYd4rKoINEXh0GgMn671DTSte5Rv5IqE/YZn+dqpTVIyn4zxS8+eZTn9w5pT53D93i8O6s56mt9+/TK317b2xPVIKXn22CyfOHmo31VDPhQURUFVBDOJKEFDp1jt75tlWjar2QKT8bCX0XSnw0JKpkci/NKzZ/mXL7+zq3ChDhFv+/bBT0xXyoGcOgEs1+357FarYA3NXFPrF/RwV0eKvZ2Al9dy3E4VOjNGSvhwNcv5yShnxvfHJ2gvp2q9yMDA79oDD6QoSgfH6dTmchgKpSgeFzceDREL+lnPFvouWk9RnicSMMgPqCA/PBbHr2u7EvEk7FmUtV2Xt24v87W3r7GZ31tmCldKzsyM8bNPne4rztcP52F6qtayfyQjARLhAIVU//F1a6lX5kZjZAchNhLmkjFCfmPXBNpx3RaXSg1gLGQ0vKcHeawr4X66xOPTsb6TUbIcVnLl3v5HEoyOfNii5Z/ta7twVfXn7jRPOziLViyn5snUeZ3rUnNR2B+0ewQPA60L0egH23F3RRAloCtKZ67y4Q9tNMWzGEX9BpOx0I5+TQJ45+4Kd9bTA6X10FWFw2PxPXEsexHxcmWTP33/Ft+9dg/TsvdMnI5PJvjVT5zvmkmgc3SHQ738V8hnMD0S4d5mtu9ZIwRcvL/ORq5EpbqzglxVBEfG4gM6fHeHZ9DZPgQ1gMdn4iznKtzcLPT113BciVWbzA9WMgQNldPjkY5YO4mX/fLdpTT3M2XA83tSmgONJUxF/RwfbbJQ1DmYriKe3L6m+U07HOiDTqPs8qB6cZz9hF/XPrLsDabt7Nr/xqer6PvgXKsq9bprCnPJGB/cX+t7vRCCB6ksi6nsjptdSkkk4Gd6JLInbnE3Ip6UkpurW3zt7evcXE15a2WPYt2ZmTF+9RPn+yYN3AtURaDUDp25ZIwf3nrQ93ohBKuZAiuZ/M5zgacLnB2N7nkumneiBhD16fzCuRk2i2bf06RiO9zdKvLW4hbFqsN3bq/z+r1NNKWTQFVtt1HDbCLs58JUjNGQr6EoVYRgMuJntDkHVCAExx6Bq+96Fr2Wh0o4ctpzmmz5eOfBkHKnxGE7xOntI40K+41dx/VZjtNR9aIfSqa1Ox8f6bXT2AO3V4fSVAVlLhlHV5Ud2zSoBUgC49EQ8ZDfW2u7RLtYsROKZpVXr9zj5ct3yZYqO/rV9e2D9AwJz5yY5eeePk0itBPntHsoQmlYRAfRCUKdZxggPExKkpEAo+HgnuIEnXrR02YRDzw/oZnYzoNzaiyCX1P4+g3vJDRtl0qXsj31yY77dX750RlmYwM4GGo6/I1/BKeegGyqSYkuIRCGT/40hLZ1QTG/5+ezE5dgOS5b5SpHCPX4vvf9roTiAHXTBkUs6EMVw/NldU/rqjW4b1K2ZO7K/C6BkVCgb0DyoKhnPQWYiocJ+31kipX98feT3kYzNHVPmUDbxYqer5OS22tp/uDdG1xb2thz6SVXSiJ+H1+6cIyXzh4+0HAWiXdY1Lm8iViIWNDHera4P24MEk/57tMxd6kSEbQW9oRdJqw7NhrGp2701c3IWorfuXiQ6WgT4bOq8Oa34K3vQKXYeaOqeVzUL/3nMDLWtx1HEiHOjEe4vJrtEjpTF9q8jr77IM2RkRCJtqyd6XKVxUypqSzz9oPq9763nGZuJMh42NfzNNEUMZC/zkgogK6pw6dNFYKiaZGvVHtWP2nHZn535YEU4YXJ7AcUZXvEYkE/iXCAdHF/8jupqsJ8j7JMw6CeuqRfi+pc07cv3yVT55r24ICJlBwZG+Hnnj7N2ZnkgXrL19HM6UUCPpKREGuZ/UkGqCiC+WS8qYO7Q13Eq2NXBCqgq+iqoGzJHQc25tdbT5k//Qr8s39YS53bs7eeP9Q//N8h3NsPJKCr/Py5Gc5ORMmb21yOwLMeZssWC+ki6wWT26kC/8/bd5mNBdFrofG2I1nOlVnJV1AVSIZ8jfa6UlKo2mwWTB5kSvzG23eJB4yeCsCQofHYdJxzU7G+SsKRUGDbhD5MlgE8E/x6tjBQNgPT9qpz7Gbx6aq6b5kMlCaLrE9XGQ0HuLXKntPXSCDs05lJ7N1PyJVuT0IupeTW6hZ/+N7NfeOa/LrGCycP8aULxwdQhu8fmomqriokI8F9014EdG2gddkXwrMcNqttds1TNqaon3glJXozV1HIwh/+BhRznjjX5z5e+xMvle5zX+jbjpCh8cTMSM/vU6Uq/+HDRW5t5lnLVVjNlTuu0RWFzxyf4Lm5Ufy62ijnU3VcLq9m+aMryxQqNvmK1fM9EriTyqMqgnOTvU/1WNDHWDTkZSYccsxt1+XOeoYnB8gZnsqXPEfHId8ipSQa9DEV3/9UK4oQHBqN8dbtpT0/q+5pPRrZ2wYX1DmoTgpVNC1eubLAty/daZSf2qsifC4Z46ceP8GFucl9EaH3grnR6L7kOXelZCQU2Beu29kvAiXrsuIwFrRy0dMt1b3Ee0HgeZOnd6hmsfYAXv09SLVZhsam4aWfg+QUo0GDJ2ZGuJPKd3WjkBJGQwbPziUIt8XfaYrK4zNxLq1kuLKe6+sJLYBS1ebSSoZHJmI9u2doKkfHR7i6tMFucH1lk3yl2pG7ux1XljZqjnLDPV/WfFlGwvuTbkRtUyAfGY8PpJwdpJ11h8O9oq4kb8ZiKsvvvXWNS4vr+1LmWwjB44en+JXnzjIaGTzgu88Th75DaXMlnB+LE/Tpe0qmB3gW+ZEw4X3IY99TxJMSUiWTXBuXoCqCsbC/tyd1X+NYW2jMwCk2BxiuSgn+xX8DL3+1sxFCwLX34B/8b2B4YpsqRFcLpZS1tMQ9PLQ1RSEeMAbO9lCyPNN+PzHv9HSSly/dGXqTKkKwtJXjg4VVXjzdu4rMRq7Ia9fu72pjqarg0UMTHZZZ2IVqQYJQWiPnp0cijEaCLG3l9rTpFUUw15S/vK7zHBqi89S+uZriN777AauZwp65Jq9tkvFYiPOHxnmwlWNhM9sgit6/2yWqHNdtKSYa8Rs8MjveqXfcRV896+h2X8ZjIcajIe6sp/fWRwFzo9vhM/WURbuB02ZxbxCoq+tZfv/SAwpmq8VKCDgxFuWXHj1E0GjiMBphKX1npqMjDY/wvWrmttbh8tseN9a+mVzXEw+zKRibbnrV7gZNEezc11p/B7HPzY/FODQa4+Yukujbjssfv3+T8ZhX2qgda9kCv/PGFR5s7ey70g5XSqbjEc7OdjdO7CokpG04wn6DmZEID1K5Pemh/LrKbGJbDN3ZlaRPE9tcTC4tbrCSznc6qu4SQgg28yW+8trFRhvrivKW7GpNDs2y6d7H5if5ay891pKtcz/iB4OGzqHRKLfX0nuaC0NVW2IGe6UAHwR1slJHg+JcWsmwUTA7ZFKJ5Pp6ltX8GEdHPQcyn6bg01RcWWWnYtgho02KbBCnPRKoethFz7g92Yjbqw9YtzEb1I+qPclm1+sGuAa8hfHs8RnurG0N724gBBu5Ir/+ynt8+sw8p6aTGJpKoVLlxkqKt24v1YJsdyMCCJ47MdtTcTuYMX7nd8wl96aHklISC/hJ7lMaknY4rrvvcYyuK3H7zLZo/au58iJXHmywsJHhzMz2wbFfJRPmknFU5f6u75dSEg4YTMQOxrnUywclPQVst9Nf1BrR7FwX8ek8Mz/K168t9/R5EAgOJ0KcmeihMN5x/sXOeqpG3F77qLUSrXod+65ckJQdsnlXDMJBDZHt4YkjU7x+c5Hba+nhSxEJQSpf4nffuoZf11AVBctxGgUSdkOcXCk5Oj7CCycODX3vsDg0Gq35Lu22cq/nx7OTHm5QtDuGHmTFlKHbBlRth9VMoYVA7Rf5nE1E8Osa5V3mepLAWCRIPDh4nrK+/W3bzlqjt/3i2mTjggZeODzGeMjHUrZENz/HsKFxeiLW5nfUJN4NkkfD7uMg2azPan+WqBGKmgPpeNjP3EiQW5udKSMMVeHMeKyrzqWjQTvG/A0+EdGAjy9dOMGvv/LerhKB1YlQuU6U2B1hqg9l2G/w00+cHNjHai+YiIWJBHy7ztONgEPJ2P5YwdpDsNhbQPdBQCIp7aOzcDOSkRAjIT+lXWb9RHr1GPfLybTZsRfarHhS1mhRlwFqh6YITk/EON2LQ+oGfxAi8cEUztKBxZu9v0+tQqnQQ8RTPJeGjWWYnCPs0/jlxw5zJ5X3CkQ08c+xgMHx5E4mddlzbFrHb7iF/ejcBC+dnefrH9we6r6Wru76zm0oiuCLjx7j/KHxfXjazogFfEzEQmzuMse1piotCvK9joXSpjz++PBPTX07ID/OsN9gMh7x6uDt4h2KIloKle69n60s1LYVry6e9OSg9gGROHzhV+DuVc8K12tE6rL4d34XTj8BF17Y9puS0nMv+K1/7hEoWa/W0kR1hAKFHPzuv4LxWRifIRE0SARH2Q1Ggr5tEbEPpJSMBIyBfUtURfClCyfYzJd5+/bSR+JN3NFm4PkTs3z2kSMf2ft1TeXQaIzLD4Z3tZASQobeofMQQuy6HJLSZmk8qBzlu4WAodLsDAO1RmDeubs89L0Sz0Fzqs2pVwh2baH11DFtHJQAEgHDM3t22YRhQyHs27u/CQA/81chOuLlHbeqnd+7LhQycOcqrC/B//J3Pb+mellx1/GyamY2IRiGpz8LR86CqnpDlkl5z753HV79Gty5DCcuQLB5ECWoOpw4D5/8KQjH+zb50ekRrqxkuL2Z780lCZiOBXlmLjnUcIR8On/xuUdwHJf3FlYPvGpGYwSkRFEUPnFyll945iz+AVj0fTBtNDCXjKEqytBcp0SSCAcYaRNFlVpivKHHgfpmauKgPl70CRBoysHVxZur6QTtIXWCUkriIT+j4Va/LkWIXYvf7eJ2Y1U+f2Qcy5WkS2aLBUERgrOTcSYi+6Sb8AW83OJf/ItdekxNY2/Blbc84rRyDxZv0WR43bbSfeFX4O/8T50VVe5ehf/xr8Pda3DvBixc694WVYcb78Pf/sd9q7KMBAz+0pNHuLqa6fATqyNgaJyZiDG2CwfHeMjPr33yUaIBHz+4sUjVcQ4sd3o9G2os4OPz54/xmUcOD0ScgH3xOq5jeiRCyKeTLw9X1lxKmIyHCRjtTrUKhrY7PUhzvCAcbBmqXUHQseH3ywUCYCIeJuL3sVUYUicovWwSIX8r86IqXmrhoU2+sg+BigcMfubcoY4TbS/K13ZkylXubRWodKnAIgTEAz7mE2F8hg8ufAIe/yQsL3gBxO1QFDj7dHfCcuQMPPsFj0ApKtDLydSF7/4B/Oxfh8On+7Y9HjB4/sjB6WiiAR+/8vwjHB6P880Pb7OcLnjpivdp7OtuFgFD45FD43z+3FGOTYwMNbfeJtmf9iRCAUYjQc/bfZgMncLzIG9vt6YohP36roKj1Ta9x49C1O4HQSeBqueb3w+FfjzoZzwaJFUYMvxKwEwi0sG5qoqy65zkSjcRr3kghpqcXBquvA2lHK2a5yScfcrL71TDUqbE77x3l6VssScLrakKTx1K8jPnD+HTVAhFe1v8hPDq1fXCxNwAITUKmGXIZ3YxlPsPQ1N58dQcZ6aTvHl7iXfvrrCcLmDadUvdzvXM6pC1DKlIL+o/EQ5wairJM8dnODGZGCq3eR2aotYcItuGscf1Th/nyYBPZ3okwq3Vrdbyse2Xt+Un1DSF2S4BwooivODXIYvCdqtAEvYb3Z8juzex6zgMS+P6PFsRdEQmaDXxuF1f1uu1bp+5MDTP0fLSg43B54JaIdlEdwX5WDRIPVHloHCk3M5XWe/nkMO4jWoF/uU/gm/9TkshA++pupcu5a/+16AoSAnfv7PKwla+r5hQtR3evLfOqYko56driem6+TnVP+8HVe19bx1yHzzaDwCjkSB//rETfPL0PAsbGa4tb3JnPc1GrkTJrGI5bi1vTueQCCHQFYWAoZEIBzg0GuPk1CjHJkZIhIN7EtPGokFeeuRwxzNU0b16jEQyEQujiE5xRABPHpliK19q6D0FrSlrpJTYrsStGUKEEEyPRHpajc7OjnN1ebOj+GPz+DS3RVU8XcmJyURL+88fGufKgw1s1/VSH6sKmqKgqV5VHVVRvPQ6jd8VNFWgCAVF2R6PepHYXmMuqZX3cr0Ql+1/vd/t2r+KgMmRVqNAIhzg02cOdwRRKKJ7WmiJVwS0l27osflJltL5hh5K0CpGSiSOI3Ga5mI8GuLYRPdA/VPTSY6Oj1B13O7bFxrZPRtzoSicmk62zNHuCVR6A979HlhWZ6hJpQRvvwJ/8e9AKErVcVjL1UpY96GoAi+53FquzPnp9m92gx3UuqImD3wMiRR4J/m5Q+OcOzSOadlkShVShTKbuVKjQnFdX6IIQdDQPaVlJOgpkoN+Aoa2byLLyanRruE1u8X5uQlOTyebtYutba2d+nV6I4QXVtHLWndmJsnf/6nn+yq5m7klIbxN2L5pT0wm+C+//BwSj3Op5/Ku60fqz/goRMG6CNf+rsNjcQ6PxfftPSenkxydGNkea+rbYlsyct3WudBVtSfxPTo+wt/78vN9RdD6OHq67u252B8Oqh5q0s0Nu06wmuKOZHvgcC80s6KJcW+AZBdeW/d71Yb7YSeHUMnHlji1w6drTMTCnnl95kfdmv2BgF2Jmr1QLwiw53YJ4aVm/hjgo9KHea4MO8zFEFPlzcXeLf97c/9smGfbB7G7aLVDcRXvmub/vPRzcP09uHWp9QqhenmiHnm6z4MGyJzw40GbHuIh/sxij/7pPWLhWlhDmsJoBuOgGpicg3/4LzxlfHN+clWD2Ghf1wC21r17+lljpfCepX88TsuHeIiHaMU+ZWkXO/x/D85v/qD3Mww2V+C9725zeL3ES8eBuZMeIXyIh3iIjx12T6AicZieh7X72zXr6pASZo+CvzVtR5c6wh1oZOpsh2PDrYtw/6b3e1f5THrc1pvfghsfeG4EuuG11fB32oFnjsCv/j2PG/sYoC4C76M/5E8s6kz5j4kKsX9f6raaH3VDPobYPYEKx+Bv/g+eo6NZaVL2S++7z/4iaJ7oJJG4rru9qnaAr11xKiX84f8L/+Z/rfks9XiGpJHBAAQ88hT80t+G+ZPg89Pmjee1c1jubJ/hSslSzuR6qkiq5IX+JAI6J0ZDHIr699V7+ycF+arND+5nyJk2T03HODKyc17yO+kS1zeLzEb9PDIePjBP/WFxe6vEuys5oj6NT8yNEDb6a6Irtsvby1lyFZsnpqNMhgdLc7KSN/lwLc9IQOfxyciBxfbtN/Ym4h0/7/3sgKJpUzAtvKwAoicfJaUXmHtsrM0Rb3MFfv/Xa/nMVfoGGQt1m0j+tf8Wzj//0Y/qgKg6Lq/dT/PDB1kKbek03lrK8uR0jE8dThDYhwq/P0m4uJrne/e2cCVkKzb/8YVpfH3GKGfa/NGNDVbyJjGfRjJoMBXZn/xFe4Fpu3z7boq76TKKEER9Gs8five959pmgW/e2sRyJZmKxa+cm0Lb4RCzHJdv3t7k2mYRn6YQ9WmcTh5Msr/9xr5WCpTSy0/UnIjMtB2+e3OZdNEEIGCoBHWtI2pZIon4DT51YprZNqc08mmPc1LUwXh66XoBxnOnWj6+t5XnwwepHXPrJEI+njg0xug+FQ7oBlfCa/fTvLKw1SgLX/cxUQSUbY94uVLyxWPJh5xUEwq1UClVCPJVG9Nx+xIot+bwCf292z9qWK6kbLm1Qq6SdJ+qQXVkKzZOLdd8uuLttZ0CiWXtXfWx+Lhla+iHfSNQrpS8cWeV126vYtpOg/SYtkuuUkUAzx2Z4MXjU0RqRQzaYWhqp3gHNOpSD5wqWHhByU2lre6l8vzG69fYKFT6O5fX3nB9NcNfef4U0QPyh1nMlnnjwXbBUV1VmAgZCAFrhSqm4yWIfWc5x5mxMIfj+1c/za4l6FfFYMVG6+NSLy5Q95AeFPUQhmHucWuhOkqX8B7R9IsrJemyhe22etb7NYWg4ZUQi/l1vnAsyaW1PEcTwZ5ikVeswMucqokhwoqohcwM2ceWvuCJb1tlq6UfioCQoWK0iGSi495+eixDVfjskVGiPo2xoMHxRHe1hpRg1xShqjJ4X4btv1Mjks1kUuDlmGu/f98IVKpQ4euXF9nqKGvt+YqORYL8ubOHSOwqY2OTv9VA9Knzog+XUmzkyzvmDKp/e3sjy71UnvMzB6NAv7pRoFi1UYTApyp86USScxMRBHBlo8Af39igZDlYjkupLbjalZLNksVSrkLOtNEUwUTYx2zUj78HJ+FKyYOcyZX1AmtFE9uV+FSFQzE/Z8fDjAW7E2LblSxkylzdKJAqWTi1Kjjz8QBnx8LE/d2XkOVKFmp6n0zFRghIBg3OjIWYjfp7LuStssWV9QJL+QpVR+LXFGajfs6MhYi3Rc0LIG86/NallTYPdAj7VL58Yoz5eKCx+DVFdCU8Jcvh+maRW1ulRgHYuF/nZDLIiUSoJ3dWthxupErcSZcoVh1URTAe8vo4FfYPpcBXhODKeoHbW6WO744lgnz5xFjL3Aq84rQX1wusFkwqlouqwGjQ4ORoiMmw0ZqZslb5WlU6+191XG5t1efKwpUQMVSOJoKcToZ76sUsx+V2uszNVJFsbY4TAZ1TyRDzsUDH4Vd1XC6vF7i6UaBQ7UwYMB318Zkjo4SaKki1ZtQEltIFbqxl+tZXF8BYJMDZ6QSBWqqOYtWmbNmeBaptBFzXE5v2lEO6QaMGnPW26ypDpkx1paRiHUyaVduVrBWrjffMxwNcmIw2dAmPTkSp2C7vLuc4FPNzpIl7ylZsXruf5vJ6gXzVpnbgo6uCuViATx0e4ehI6wnp6boyvPEgQ7FtHK5vFnl3JcdLhxNcmIy2WBBLlsN37m7x3kquo8z91Y0C761k+fzRJKfa9BmZisW372xxeSOPabfGab67kuPxqSifmh9pKWXmSskHq3m+e2+LzZLVYsn9cC3PO8tZPnd0lDNjreK/KyX5Los9XbG4tllkPh4gW7H5+q0N1gpVbm2VmI76GlzUasHkT29ucjdT6hB9Lq7lOTEa5IvHkyTbCPhqweQbtza5ky61VFi5DLyznOW5Q3Gen40PpYyuOi5me04mCZfXCzwzE2M2un24C+ER8z+6sd7WbsEPH2R4ejrGC3Mj+DWFquPyrTub3EyV8KkKYyGjoYPKVmy+cXuTKxuFjhzxl9cLvB/L8cXjY8zFWhmLbMXmW3c2ubxeoOp0mePJKJ8+kmgQG9uVfOfuFq8vZrDdJkNWU0cXcxWmI36emNrWQbcQqFvrGb7ywxtsFjqr77ZDVRReODbJzz9+rDEJjaILXUJTtCHFgk708lrvdmmvawZwZf8IIJt0IgBhQ21RdCoCnp2Jc2EyiqFsi2HpssXXrq1za2s7VW7dJ9Z2Jbe2imwUq/zM6fHGApQSfrCY4dWFFE4tyLN58wsBWyWLP765gaoIHp3wEvvZruTlOyneXMrWrmu+z3v5St7k96+v84vqRIMoFqsOf3B9g+ubhUYdtjozL4SgZDm8dj9NxXL48smxhujy3kqeP7m5gWm7CNE8TZ6mcrVg8ic3N5kI+wZKzxLUVaZqRMh0XE/XowiqjkuxRtDq47mYLTfS/jbaisCRkisbBUzH5RfPThKtFXbdKFb5vatrPMhVOu8Tgrzp8O07KWxX8tLhxJ4thuMho/Hu1nW0PULb74dC1eGVhS2qjssXjiexXelxeEJgS9ngEsuWwx/f3ODyer5r/yWwkCnztWtr/PIjkw2iXqg6/MGNda5tFL1It7b+V2yX1x9kMB2XL58cw6cqLGTKvLmU9cT9HmlifKrSwj1BG4F66+4a6/nSQHoJx3V55946zx2dZC4RoZG3u0v1EzlIjEs/1DmngevpdV4n8fQng2Yv3u+yQ0N3WdBivbNdySsLW9zaKqIIb/GEdZWIT6NsOWRNT1zMmjbfvL3JeMggEdB5kKvwxmKmUdgipKscTwSJ+TXWi1Vub3kcQNly+e7CFvOxADG/xo1UkfdWco1gzphP4/hokKCmspQ3uZfxDrFsxeLVhS2mI554+dZylhupYm3Bw3jEx1zMj+NK7mbKpMueIvj91TxzsQBPTEdZK5i8spDyuC3hcYNH4iHifo2NUpV7mQqOK6k6rkfYRX1Ovf68MBcnUNNd1pdZIqC3cJ7dZvP1BxkWs5UGAZmO+jgcDyAl3E6XWC9WUYTgzlaZt5eyfPboaGMe6sSpft9M1E+p6nA3XaJkuzgSXl/MMB8LcCyxsyuLlHA0EeTc+DaH6AUrw1w80JVA1TEeNpgI+6jaLovZMiXb01++tZRlPh7g8Ej393+wlufaRqGxnkaDOscTQXRV4V6m3OjjWsHk+/fS/PyZCVRF8MZihuubHnGSQDKoMx8PYEvJQrpM1vSKgHywmmMu5ufJ6Rh30yVPNy0EMZ/GY5NRDHW7jpTEI8Tt+rFGr10pvRLIA8b01jMPlJtEhobaq8v9ZcvGdl3UA0xdut24PpzWgITy45Y4fyVvcnXD40okcHQkwBePJUkEdEq2y5sPMvzwQRaQrBeqXFzL8+nDCS6vFyhUbYQQhHSVnz09zulkGFHjun6wmObbd7a8+4pVbqdLPDEV5eJa3kuVIQSJgM4vnJlgrqbPMW2Xl++keONBBkUIFrMVlnIVJsM+Lq55lXOkgKhP44mpKMmg7hE5v8brixkqtovtury3muPcRJjL6wXSZU89YKgKXzye5IkpT+Q1be+6O1slTiZDjIWM7dNXesrwxyajfTdwt+WRrdhc2yjWyqrB6bEQP3NqvPGczZLHJd3LVABPL/jcoTipksWNVLExD+cnInzpeJKoT8OVkuubRX7/+jqFqkPZcri8XhiMQCGZDBs8PTN4AQIJnBkL8eUTY8T8Oq6U3Nws8QfX18nVrJsfrOaZjfo7doPlSC6tFWoGDMFM1MfPn51kIuSJsoWqwx/fWOfiurfmbm+V2CxV8WsqF9fzDWHk8EiAv3Bq3JsXYDFb4WtX11gvVrGl5OJagQuT0YYVsT5nh2L+BvesKhDz6UT9Wkc7ta7dHnRvNrFpIUMnoKmUTKvNE9o7SdeyRe6n8pyYiA88AR2ralAOqht9ai9ZOsgzPkY0ajFXoWx5HEbEUPlzx5NM19IwB3SVzxwZZaNY5UaqBEjuZcqULYflfKXWfcmJ0SCnasQJPMXpU9MxrqwXeJAzAclqwaRYdVgrVL2Zk94mnG/iRnyawrOzca5uFshUbCzHI26aIsiUrcbzC1WPm2uIImI7570Qgo1ilfVilaV8hXpys5OjIZ6cija4eJ+m8NxsnGdmYvvmXCkQbJaq5Ks2CDBUwXOz8RYilwwaPDMT50FuDUdKsqZNtmKzUjCp1PRqMZ/GS4cTjfsUITgzFmYhU+a1+xlANK7377MvW517/NR8omE8UIXg9FiIhWyE799LIxCsFU2KltOyb4SAnGmRKlcbKU+enI41iBN4aofnDo1wa6tE2XYp2Q6psoVPdciZ9XFT+NR8grHafQKYi/l5ZjbGH93YAOmNc6FqMxHaVtqvF6v8u0srLWlvwobGs7MxnpmJt6g7OgjUIOWVuiEZ9vPFR+Z57dZyi7LNdl1y5Sr5SpV/++Z1LswmidWrpODpso6OxZiO93Ec8websmcO4gcFBMKgt1p9humb+BETJ1d6HKquerq7gmk3MmSOBo0Opa1fU5iLB2oESlC0HMp2XenqUduYX+8Io/FrClG/DjmPkFmOJ0pVm8pzdbPUhQ2VsKGRLtu1+1zKtttSdMNtL8LRPKbS4+Aqllvb8F4bk0G9q4pBadlgouUdu3FrKttuI7GdoShEunBgIwEdXRE4jmz4UpUtp8bBCcKG1pVzGwsZnvgjPW6zV5ny9ph6Z5iO1CxtiUBnSpPJ+vtr82k5smXXCMCsfQ4eYes2xzGfhl9TKVtu41lSbo+bX1MYDXa+fyxkoCkCy5VYrqRiu5xOhnk/nudOuoQiRMuYCDw96Ddvp/Bram8leSNn0y44KCEEnzg+zRPzY42XCzy/khtrab76zi3WskW+kS12JCWbjof56586x0S0Bys8PuPlGP+Tr3hVXXZCMOxVazH8za8Zqm9SHJyYJ4RoOSUKVQfblY3PXCl5fTHDpbUCszE/nz86iq7WfV88JWTVkbRbf8uWQ92TyzNKtL23V3v6tbXX9108PgKagqoI3NrC1xThGVDakq7VHSWPJ4JMhI0ad+F9tlGq4riyLbOm559TdxEIG2rj5C9aDutFk1jbBvM2nxzMitYrMKFL5/3advbQfNUmZ9r4tdbDYqNYbfi3Garo6emtq4KgrtbWmWCtUO3gtqT0rHuaKlp8ByWtyfda2z3IKbxDPt8ec+zTlJpTsaRiO6RKVgeR3CxZDRqgKV6G15Ch8jOnxvnO3RT3MuUWAmW5nl+U5XiW60fGwg3Xji4clByISfG62KqwEgKCRidFfXJ+nPfvr/PB4kbtJGwaHAEr2QILm7neBEoz4Nf+Psyf8qq0yD5VNzQDzj3jlaNq9Akqtj1U3/atFmC3JiqCsaDBzZoyeSFT5r2VHI9Oen5Ql9YLvLKw1RDRjiWCjId8qIpASm8DvL+a44VD8QZnsZitcHWj2LCQjIUMDFX5yEooecpSg7hfZ71QhRrb/tmjCcZDPqSUZCo2rpQkAh6XFPfr+DWFmaif65slFAE3UyXeXs7yxFQUXVUoWQ5vLWW5vVXi9FiY52fjzNQU8hXbpWq7fPdemrChMV7Tg9zPlvn+vTSOlLx0eHTH+LZhMBH2YagKluOSN22+d2+LLx5LEvFpOFJyY7PIxbW8Z4mUksmIr6cflaF6fb+b9vr+IFvhtftpnjsUJ6ApmLbLO8s5Lm8UmAr7+MKx0R+p6UYCowGdkKGSNW2qtuT797cYCXjhQ1J6qogfPsg0At+TQZ2Izxv/8ZDBL5ydJFO2GjopAby/muMHixmEEGQqFkXL6UWgZNPfA7R2wMWvKgphn9FbxJIDlPqJxOHLv7argc1VTJbThTbzdZ+uSe9025Pf1g44Ox7m/dVcQwz705sbvL+aQ9T0FnUFta4q+FSFZERnLGiwWqgikbxyd4vNYpXZmJ9cxebDtXwjVMKnqTwyFu7qrX9Q8Apqqjw6EeHlYgrw/KFeXdhiNhrAcV2W8yaOlHzhaJJHJ7frFJ4bj/D+Sp502cJ0XL5+a5OrG0Vifo2Nmo7KdiVrBZOjIwGmoz5OjIb4cDXXIPD/5sNlxsPeGlvJmxRqPmI+VeEzR/bH2daVkpmaVbJ+uHywmmetUGUm6qNQdTzdn+02UgIf6qKgbsajE2EurubJmTYOku/d2+L6ptf3vGmzWqhiuy4PchWOjAR+pBVnvMK0XiD7W0uegeROusxvfrDMXDyA7Xi6z1zVrhFomIlsK8MXMmU+XM1jue62lVzAWsFsGCvqDrV1tBIoyWBpL5tvGPDa+olyEFKT48qW/Nx1uBKyZZPv33zAcqYAElzp9mSN69AVhacOT3BkH0s6t2Mu5uf5Q3FeXdjCkR6bW7cYKdtzxxNTUeZiXlaDF+dG+IMb61RtT0/09nKOd1dyLUpoCTw+GeHoSJBq23gMNvStfHHPc6hlKrd/e2omxlKuwpWaj8xm0WKj5pQqhMBxJZc3CpybiDT6OR4yeOlIgj+5sUG5prO5tVWkPgpKTdQIGRo+VUEVgpcOJ0jViFfdvSJTI9BCCK/SMJJ4Te8mu3Wg/luf5BgtA4EXkvTJ+RHWC1WypoUiBCt5s2GMaF5broR7mQqPT8V6ptCZjvj59OEEX7+92XBq3X5eXUwXBDTF0/k1xesNs5Xa1H9d+ix3vK+OFw7FWcyWWS2YKEKQKllslrbnuLmri7kKpu0V+fj6rU3uZcpdVA+itnYlR0aChJtqHrZ5kstBM6J4E+BKrAGLHDaqlu70bLMMH74Om6ut4pimeyl+pw63XJ4uVfjTD+9yc83zUG2+xZVQqloNj/CxSJBHD40xFgn0rXwaDfg4OhbDrx+cS4QiBJ+YG0EVgtcfZMibdf2R1+6Q4ZnoP3V4pKGPOT8ZwXRcXl3Y8iwpiKbzRKIrCo9PRfns0VGvSgae8tfNVdCU7opQRQgihtoIdxypiV1hQyVdtjwlehdFsKoIwoaKKyW6IogHvGtCuspPnxonoKe4tN7qSS6lJGyonEgEOxbpY5MRVCF4dWGrsdjrC0YgOBQL8IVjo4zU9B2euDDBy3dS3NoqtXgzSynxayqPTkb41OEEjisxVMXTF6kKhioQQmuIkGFD7WplC2gKPk2haDkEfBqhmqh4dCTIz54Z5+U7KVbzJm7zopbe2NTjD1fyFSq20+I1344nZ6LoquB799JslqpNuk+JRJAM6rx0JMGRkUCNKHoGhpCudq2mHKo5/tb1WUHd659T0+P5NZWgrhLQa/33qYSMzjk2FE9HtlmqEtTUxjoYCxn8/JkJvnk7xUKmjC2bx97jgjyHTM9dI2fahAy1Z4UXWfNLOTIS5MW5eMvaaLRKEYKwTx/KmdFyJWvZIudm+pf7rlg2m/lSvSld0Xjl1/8d/OY/6V4W/cxT8A/+GcSTjYX4jUsLvHr9QeOk6XhujdWM+HV+9bkznJwcGaxzHwEMVeHF+QTHR0Nc3yywXqw25PxTyTAzUV+LmKYKwTMzcWaifj5cy/MgW6Fiu55OK2RwbjzM8dEQem2GdUXw+aOjJAI6Mb/GufFI13a8MDeCriroiuCJ6RhBXeVLx8e4uJ5nOuzrCJ0BbxF+9sgocb9O3K9xJrntYBj1afz0qXHOT0S4tVVquB6MBHROjoY6wibAW38XJiPMxfxcTxVZynmxeIGadfLEaJBI2yaaCPv4xUcmuZ+pcDddIl3xAm09MSTIXCyAVtPbvXQkweX1PEfiwZo3Ovz5E2Ms5Sqcmwh3JcKjAYMvnxjj5laJE6NBRgPbIv/J0RATYR+3UiXuZz2XDqXmM7ZZqnJ10+MAB9GEqELw+FSUIyMBbm+VGi4luiKYifo5mQwxWiPMp5JhXpyvkipZPH8o3jCeNGMu5nFli9kKT0xHGQ3qfHLeC3tJBg2OjQTw616s4o1UiWMjASbDneqMgK7ypRNJLq7lmYr4WlxNZqJ+fuXcJLe3SixkyuRNu+GEaTou76/mGp49Es+z/3NHR7m4lu+olacqgpmIj3MTEWJt8yBkk8/5zbU0v/mDK6znOgMWu0EImIqH+aWnTnI4GevgSiSSkmnz2s0lvnFpActxtgtKbl/ESMjP3/rsY8wnwl658x98vUs1Yem5G/z3/xpOPQZ43NE//cY73E/l+/rIuFJydmaUv/WZx3padaSUFEyLalPMmQACht5RZvugMExGTQlUbRerVjfNryn75ie036g7fw9ZVRvZpahm33t2GD/HlR1lzodF2XJ4cynLak1vImoWrXofXSlZzFYoWg6ulJwbj/DL5yaH0gc2+t6jUGt9D/Wb73qGgeb3OrJ7dohhYLuSd1dyLKTLjTa0lAYDlvNmI+h4NurjP3lshkCNg3S7iGj9CtK27LwTEyP8jZcucHNta1ska3uUK13y5Sp3NrLcS+VYSRf4v179kLFokICutbzIdSWZsslmvozjuoR9BkfHYiTCgYZeQFUUzkyPMpeI1lwI+jlkihYLXr244U5nlJSSqN/oKdZJCa/dWuaVq/c7AoQTIT9/4fHjHJ84eM5rpzqjbSOBT1P40add2xm7SWVV3/xD3bPD+O1HTq3LGwW+dSe1Y703pGesuFATXfez70LsHIol6KxGvB9Gk3uZMl+/uUGlh7jW6D9emM6FyWiDOAE9JZ1e6GANZkbCzLQnjOuCQqXKV964yrv31ihVLRY2vDCLXo0N+w3+8vNnOT+bbKlY2qOHXTzBexCtOhvZp88SWUuQ1x3pUoWvX7zDeq7UcSqt50pE/Pc5MhZ/mDTuIXrW32i9RhIyND45P8LJH5PMlQP3f+fuI6XEpyk8OR1rcbrcDXYtu4T9Bs8eneLiopfuQfShjK6UnJ5M8Ojs2I75mHpmLeh72w7WwR1GtFy1qdRyM7W/RhEeMXY+qjjCh/hY4+x4mKxps1Wqdj0wBZ7n/alkmNmo72Mrdu8W87EAnzs6ylKu0lNmrxtCjowEd0xHvBP2pFwZCfkxVJWSY+1APyQTseAAxIneeZ9asmq2fb6j6XFA94YubHs/Vv4h/uwhpKt8/uho3+X0k0WSWqGrnvX5o+r/niIY67Xq6+4JvX4AfNoAtFBV4fBpT0Huup0/k3OQnGq5RTb+9H6/lDQU9N3hyYndngMHT6RcKclUbZZKVVKm00iNcpC4m6/wdqq0HWX+EENB9Pmpo2A53M6blJzBXHF+nDBI//cDezdPDcLBSLmdzG4nfPnXYHQSUk1+UBLPD+rCC953Neiq4mX03IGACCTpYoWq43TNee7WE+l360cts6bluBja/ot4Jdvh3VSJhZozowQeHw1xLnaQBRsk17IVTCm4MEAqkB81HhSrmK7kSNj3Y1Uz8E6uwvuZCl+YiRH8MSnz9HFDVwJVtZ2OlL+qImpWutYVsh0c09/kOfA5HY7B535xoEv9usazx6ZZ3Mr1TOlbN1MvbuW4trzJhbmJjmvub9bvlx0+GvVUMR/cX+OpI1M9LYG7sTpZruSN9QILJYvHEiHmQzqbFasRGmC5knwtVUZEUxr+TRXHqwRiu5Ky6xLVVRQgV6sQEtYVRO26uhm4ZLuEdBWfIjAdl5zlMBX2o9esqSXbpey4BFSFUM2pr+JIfKqXx9t0vCRsAU3pSNZfsB1MVxLWVPyql7XSAfzqdjskEKjFBtavD2kKgVpfy46LVu+T4xLRVXTFy8rwzmYBVJXxgE646f0Sr9CArghM16XqQkRX0ITnKFh1PTN7yfaepwovi0HRcTEUQVhTGwTPlZK85eICUV2pVVppHZdg7d2O9ObFlTTa6UhJzvK49PpnUyGDmF9n3KdRdVxkbS0WLIdgbaya3y2BkKZguRK/qvxYEeODQgeBuruR4Q/fu8VWodzCweiqwjPHpvnM2cNN1qyaaDQAB3VQuZVeODFLJODj5mrNNaKpaabtsJLJs7SVp1Sp8pUfXObS4gbxYI07EZAtmXxwfw3LcfDrGvHQduyQ5bhkyyYl0+K337jK6zeXCPuNjoUja97D88kYzx2fITxgDN9S0eROocojiRCPJ7xkcKP1tLJli7dSRXKWl8JkPGDwifEwKpJXVnL4dS9gM2u5nIj6QbrcKVTRFIXPTEUZ1RW+v5ZHCi/p21bVYSpo8NJkmKLlUnYkozX/rqvpEtfzJlKCCzydDBPTBC+v5jgdD3I4qPPKap5kwODpZLAxxJbr8n6qxGLJqrmMCF4cj3AvX2a54vCF6SiqlLy6miOg6zyXDHI5XeJuoVrzVxI8Px4mril8ZzVHSFdJ1/p0KhbgsRE/31vLs246qIrL99YKfHYqgl/ZzibwndU8MUMjZVoUbcnZkSBPJQLczVW4nDPxK4Kc7fKF6SjrpSoXMxUsV+ICp2IBHk8EqDou72wWWag5yp6OB3lixM+1TLllXJ5Khpn2q7y5WWS14mWNPBb1cybq482NAmumV83oRCzAmajBu5tF4gGDQ0GdH24UqEjPNWa9YjMW0PnMZAQdeG+ryPWcFzaSMFRMCZ+ZjBB5WA+xM93Kd67c44N7qyhtrgBSemLSI7NjTMU9NwRNUTzF9wBZAowDGmxNVXh8foLH5zs5IwmUTIuXL9/lTz+4TaZY4dWr97q2dSoe4ReePt3icOq4LquZAl99+zp31tNcX9nsK02+eXuZ5XSev/T8I+gDiIP3C1VUReFYxNfSpLLt8Np6HhSVz0xFWS+ZvLFZYj7sY0wXrFVsAo7kwkiAq+kSlzJlTsf8PBLz81aqRMq0CSoqa2ULFIUnE0HWy1VuFUzS1SB5y0YCCZ/K/UKFd7bKPJEMkdAVvrWS416xyvPJIIYQ3MxVWC2alFw4FfO3KC2vpsvcyFd5cSKM7bh8Zy3PasUzmGyZNqYjKVs2y2WbZ8IB7uTKXMpWeGE8giZdXl7Ns1y20Pwaa2WLcK1Pl9MlHpSqXBjxM+HXeFC2OB0LcDhsYDSdDpmqzXrFxhWCxxIhLqdL3M5XOBfzs1GxWS1bnIj6eSrmFU54faPIsViAs1Ef724WuZwpcyzi43a2xI28yZPJEEFF4CK4XzA7xmWxVEW4KjdyJi9MRJgJaLjAcrHKjXyVT05GmPJ7nxUth7WKzXjIj+VKVssWJReeGA3iU2ChZFGyXTKVKh+mKzwyEiRpKLy+USCgay39/LOMFqrhSknJtBpiUfOPIgRV225J8ZsIBzg5mcCRLk4tp0v7j+24JCMBjk8mPvLOCSDk0/n06XkmYqEmz+TWP+BxYhfmxokFfYR8OiGfTjTg4+TUKJ975DCaoiDwksf1+gHJe/dWvcDkHWC7kozleEGgbcR7pVQlVXU5PxJgwq8x5tdRhZcTKWs52BIuJIIcD3uhMKN+jWeSIRKGiqoIQppCvupgunBuJMjpmL/BmUkp2TJtDFUhqApu5kyqrmQhb/LDzSIhXeNYxEBXBLNBnbWyxWLZ5ulkiLi+3U7TcbmVN7Gl5EqmzPvpMlNBg0NBg5Cm4EpPVLuTrxDQVKYCGjdzJrYLN7Jl3tkqkwwYHA4ZpKs2LoLHR0Mcj/jQhSCgKvhrP5qicDzqZzqgtyzYtOmVeno6GeZYxEdE80RIS0q2qjZJv85zY2GOhA3uFUx0VeXCSICET2PEUL3oAcvmTt7kUNjHuXiA41E/xyMGt/NdxiXsw6cqaAIuZ8qsVWxiuloTg+FSusy66RDTVbJVBxvvEChaDiVHcjIW4Hw8QFBVMBTvntt5k4hP40IiwKGQQUBViBsPCVQdPVL+dnJE3RgHQ1P5hadPMxL082Ar1/WaiN/gxVNzzIx0jwP7KBDy6yTCAR6kcl1dN1QhSEZ6F8Ycj4YwNJVS1drRSlG1HQqVKjvDK+LgSlHLpCgaOvpMrcZavBaculmxQAjiuspKsYKhKkwFdCqOS952ORIN4FMEW1UbTQiiuspSoYqiCKZrMVwbFYuAphJQBSnTIayraMJ713hA52zcj19ViBsqAVXBdFzWKzZ6bcD8bTFfZduhYLvMRfwcDRkENO9eQxHka9zjYtFkoWhxZiSEjqejmQ37OBH2EdAEcUPDpwhuZGz8msKkX6Nsu+Rsl8O1rKubpk1AUzrEHSkhZdpEDY0xn+oVvaw6xH060pXkLJf5aICgKhqHQVj3dF6OlGyaNmFdBQlFR3LMpzWIn+VK0j3GRUr43FSUd1JFvr9eIGpoTId8fG5KeJ+t5YkZcTJVB00RxHSVdNnEAWZr+dS3TIeIrqEKyFYd4n4DvyJYLFTJWi7HYtpPtKvCMOhtxeuiLO5m5RoNB/jFZ073TGuqCjGY/9MBQtCUjVB2vaBvfJKm1BSWsr8xoOfzezxz3K/zYbbCe1sl5oI6GxWbhF/HpyrYrmS5ZFGo2lzKVpgJ+Rg1VC6mvI0VVBU2SiYVR5KoJQRLmQ4B3SNCW6bjFc8smCwXqRGKYIMoTYf96DVO2ZJeUK7jStYqNrMBnfdSRTYtl2eSId5JefqZyaZsCPX4KS+q3ov4t6RkJqAT0BQU4GK6zIhP51TUh3S9NDd2TTledlw2KjZTAY2UaRPRVQKawkapSsWRjPo8DidvOTiuZKlU5VDIwFdbS5brkq1xJrfzJumKRdZ2eSzpo2w7lB1JokbgFeFF5qcsh+VSlbRpsVKxeXIsTEirlbUqWdzWFHKWw7GID1V0jsu4T+NuwWTMrzPm00hbVSq2w5VylaRfJ+nTyFhVkJKUaRPQVI9LNW10RSGmK1Qcl4zlMBvRvfEXgqzlcDVb5kq6jAuN+XyIbgRK9i7P1CsXgZdY7eNN82W/fu1gZqznAveyQuwc9zcozo0EKbuSxYLJg2KVmKFyJOJn0qeyUra4lC6hCpgIGjyZCKEILzhzJqCjCai6koRfI+nTPG9+YDZoIKTHAUR0hcWCSdlxORr182g8QNVxCGoeB2aoCudHAryfLvPKSh5NEZyKBUgpFqsVm8dHQ5wMG2xWLEqWiy1Bq41fxFA5Fw9wPWfy8oqFoSgNl4WgpjDu1yi78NRokKAqkIrC+bifK1mTb6/m0BXB+ZEQTi3V8WTAQMUTHRN+jVGfZzGeDOhs5Uxu5E1mmvKwl2yXou0S0TUupUu40nPPOBI2eFCokPB54wKeeuL8SIC3UiV+sF5AUwQXRkOcjvpQgDMxPzfzJm9t2hyN+glpXt+ax+VkLMC4T/KgWOVSuoymCJ6sidVXMiUu1j57KhkiWhOFZ4NGzaII00GdoKpQrNot43865ufDTJkbORNDFfhchegBpvn5cUNLNgNXSv7Vt9/lh7ceoIrOzAQRv4+//9PPM5vYW3zNRw3XlfwfL7/NO3dWuifkVwR/6/NP8dj8ZNf7H2zl+Cd/9DqFSnVH1ltTFf6LLz7DI7NjA7XNqbkA1M3wdVcC25WUam4CwZrJWQJVR6IqNEzptuvlvRZ4BEsRXkXg31/McDIe4tG4D8uVBDXPzO5Kj/vQle1nVmyXquvldapzDLaUGLVr6lYvoy2e0ZWee4BXRt0r4V7/vup6x1nzPVJCqXa9oQr8NWNEvz45UlK0XQxFaREz7+crfGutwGemokz4NC+lR5MbQPMz6jAdl4qz/e7txHLeO0TbWDePi7/GFVq1eTGU7WdUa64R7Z8pwutTtXZ46IpoGX+QPKhbQIF3UkU0VeXPTUcbB8GfdbRwUIoQfPL0HKl8qUOPIoTgqaNTTMR2DiT+uEFRBCcmE3x4bw2rLQpbIpmMR5mO99eRDbdeBueiVCGIdDkxNUUQbYv9E4CvaZOqQqA2XVJXrJZsF4Qg6VdriuamsRBeGtzmZwY0hUDbu5u1IHoPEV0Rnt9ON3RT8ooe1/frk1rTqbUjVzMwxA2NYNsz25+x/R6FbtKT0mUOuo1LfSxibfNiKAKjy2fdfm8ef9ORXEmXyFheNeWgpvLkaPAhcWpCh4h3dmaM+WTMqwLa9LkQgrDf6JrB78cBnzw9T8hnsNpkYZN4/l3nD40zHusfdT5ctMuPdoUlfBpfmokR+wkWFY5E/UyH/cR+jH2FfKrCpyajFG0XRXgOng+td63oqiQP+QxCPw6JhoaAX9d44eShXd0b0DX8uka+Yvb1FpfSs2weZLGFQWCoCsmf8NCKkKbyk5DIJKgpHRzgQ2zj4cgMgEQ4wPMnZ9EUpae/Vz3lzFNHp5n6EbpUPMRD/CShRUn+EL1hWg7vLqxwZy3dKDzZDCFgNhHl6WMzhHz6Lt7wEA/xEO14SKAe4iEe4mOL/x921NT8t1yQKAAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNi0wMi0yNlQwNzoxODowNi0wNTowMNbsReQAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTYtMDItMjZUMDc6MTg6MDYtMDU6MDCnsf1YAAAAAElFTkSuQmCC";
	
	public CepHttpServlet(CseService cse, Class eventClass){
		RequestSender.CSE = cse;
		cep = new CEP(eventClass);
	}
	
	@Override
	public void run() {
		
		//setting credentials for login CEP
		try {
			Path iniPath = Paths.get("configuration", "config.ini");
		    Charset charset = Charset.forName("ISO-8859-1");
		    
			List<String> lines = Files.readAllLines(iniPath, charset);
			
			for (String line : lines) {
				if(line.contains("org.eclipse.om2m.adminRequestingEntity=")){
					String userPass = line.substring("org.eclipse.om2m.adminRequestingEntity=".length());
					userName = userPass.split("\\\\:")[0];
					pass = userPass.split("\\\\:")[1];
					System.out.println("[CEP INFO]: Username and password for CEP http server has been set");
					//LOGGER.info("Username and password for CEP http server has been set");
				}
		      }
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		server = new Server(8081);
        ServletContextHandler context = new ServletContextHandler(server, "/cep", ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
       
        //CREATE NEW CEP RULE (HTTP POST)
        context.addServlet(new ServletHolder(new DefaultServlet() {
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            	
            	String deviceName = request.getParameter("new_device_name");
            	String dataName = request.getParameter("new_data_name");
            	String rule = request.getParameter("new_rule");
            	
            	addCepRule(deviceName, dataName, rule);
    			
            	response.sendRedirect("/cep");
            	
            }
          }), "/create");
        
        //EDIT OR DELETE CEP RULE (HTTP POST)
        context.addServlet(new ServletHolder(new DefaultServlet() {
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
              
            	String action = request.getParameter("action");
            	int id = Integer.parseInt(request.getParameter("edit_id"));
            	String deviceName = request.getParameter("edit_device_name");
            	String dataName = request.getParameter("edit_data_name");
            	String rule = request.getParameter("edit_rule");
            	
            	//EDIT
            	if(action.equals("Update")){
            		editCepRule(deviceName, dataName, rule);
            	//DELETE
            	}else{
            		deleteCepRule(deviceName, dataName);
            	}
            	
            	
            	response.sendRedirect("/cep");
            }
          }), "/edit");
        
        //SHOW ALL CEP RULES IN TABLE (HTTP GET)
        context.addServlet(new ServletHolder(new DefaultServlet() {
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
              
              ArrayList<CepRule> list = getAllCepRules();
          	  
          	  response.getWriter().append("<html><div style='text-align: center;'><h2>CEP RULES EDITOR</h2><table cellpadding='5' border='1' style='margin: 0 auto;'></div><thead>");
          	  response.getWriter().append("<th style='width: 100px;'>Id</th><th style='width: 200px;'>Device name</th><th style='width: 200px;'>Data name</th><th style='width: 400px;'>Rule</th><th></th><th></th>");
          	  response.getWriter().append("</thead><tbody>");
  	            
          	  if(list != null){
	  	  			for(int i=0; i<list.size(); i++){
	  	  				response.getWriter().append("<form method='POST' action='/cep/edit'><tr><td><input type='text' name='edit_id' style='width: 100%;' value='"+ ((CepRule) list.get(i)).id +"' readonly='readonly'></td>"+
	  	  																					"<td><input type='text' name='edit_device_name' style='width: 100%;' value='"+ ((CepRule) list.get(i)).deviceName +"' readonly='readonly'></td>"+
	  	  																					"<td><input type='text' name='edit_data_name' style='width: 100%;' value='"+ ((CepRule) list.get(i)).dataName +"' readonly='readonly'></td>"+
	  	  																					"<td><input type='text' name='edit_rule' style='width: 100%;' value='"+ ((CepRule) list.get(i)).rule +"'></td>"+
	  	  																					"<td><input type='submit' name='action' value='Update'/></td>"+
	  	  																					"<td><input type='submit' name='action' value='Delete'/></td>"+
	  	  																					"</tr></form>");
	  	  			}
          	  }
  	  			
          	  	ArrayList<Device> devicesArray = getAllCepDevices();
          	  
  	  			response.getWriter().append("<form method='POST' action='/cep/create'><tr><td></td>"
  	  										//+ "<td><input type='text' name='new_device_name' style='width: 100%;'></td>"  
  	  										+ "<td><select name='new_device_name' style='width: 100%;'>");
  	  			//ADD AVAILABLE DEVICES
  	  			for(int i=0; i<devicesArray.size(); i++){
  	  				response.getWriter().append("<option value='"+((Device)devicesArray.get(i)).deviceName+"'>"+((Device)devicesArray.get(i)).deviceName+"</option>");
  	  			}
  	  			
  	  			response.getWriter().append("</select></td>"
  										+ "<td><input type='text' name='new_data_name' style='width: 100%;'></td>"
  										+ "<td><input type='text' name='new_rule' style='width: 100%;'></td>"
  										+ "<td><input type='submit' value='Create'/></td><td></td></tr></form>");
  	  			
  	  			response.getWriter().append("</tbody></table></html>");
  	  			
            }
          }), "/*");

        //LOGIN PAGE
        context.addServlet(new ServletHolder(new DefaultServlet() {
          protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        	          	  
        	  response.getWriter().append(
            		"<html><div id='login'><center><br>" +
    	            "<img src='"+cepLogoImage+"' height='90'><br><br>"+
    	            "<form method='POST' action='/cep/j_security_check'>"+
    	            "<table><tbody>"+
    	            "<tr><td>username: </td><td><input type='text' name='j_username'></td></tr>"+
    	            "<tr><td>password</td><td><input type='password' name='j_password'></td></tr>"+
    	            "<tr><td></td><td><input style='float: right;' type='submit' value='Login'/></td></tr>"+
    	            "</tbody></table>"+
    	            "</form>"+
    	            "</center></div></html>");
            
            }
        }), "/login");

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__FORM_AUTH);
        constraint.setRoles(new String[]{"user"}); //String[]{"user","admin","moderator"}
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.addConstraintMapping(constraintMapping);
        HashLoginService loginService = new HashLoginService();
        loginService.putUser(userName, new Password(pass), new String[] {"user"});
        securityHandler.setLoginService(loginService);

        FormAuthenticator authenticator = new FormAuthenticator("/login", "/login", false);
        securityHandler.setAuthenticator(authenticator);

        context.setSecurityHandler(securityHandler);
        
        try {
			server.start();
			server.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopThread(){
		try {
			//H2DBTableCepRules table = new H2DBTableCepRules();
        	//table.removeAll();        	
        	//table.close();
        	
			server.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendEvent(DataInterface data, String deviceName){
		cep.sendEvent(data, deviceName);
	}
	
	public void insertDevice(String deviceName){
		H2DBTableDevices devices = new H2DBTableDevices();
		devices.insert(deviceName);
		devices.close();
	}
	
	public void addCepRule(String deviceName, String dataName, String rule){
				
		//Add cep to database
		H2DBTableCepRules table = new H2DBTableCepRules();
    	table.insert(deviceName, dataName, rule);
    	
    	//Add cep rule to CEP
    	CepRule cr = table.get(deviceName, dataName);
    	cep.addRule(cr);
    	
    	table.close();
		
		//SENSOR RESOURCES (CEP DATA)
    	String targetId = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME + "/" + deviceName;
    	Container cnt = new Container();
		cnt.setMaxNrOfInstances(BigInteger.valueOf(10));
		
    	// Create the CEP_DATA container
		ResponsePrimitive resp = RequestSender.createContainer(targetId, dataName, cnt);
		
        System.out.println("[CEP INFO]: Rule: '" + rule + "' has been added to device: '"+deviceName+"' with data: '"+dataName+"'.");
		
	}
	
	public void editCepRule(String deviceName, String dataName, String newRule){
		H2DBTableCepRules table = new H2DBTableCepRules();
		CepRule cepRule = table.get(deviceName, dataName);
		//Edit rule in CEP
		cep.editRule(new CepRule(cepRule.id, deviceName, dataName, newRule));
		//Edit rule in database
		table.updateRule(new CepRule(cepRule.id, deviceName, dataName, newRule));
		table.close();
		
		System.out.println("[CEP INFO]: Rule: '" + newRule + "' has been modified in device: '"+deviceName+"' with data: '"+dataName+"'.");
	}
	
	public void deleteCepRule(String deviceName, String dataName){
		H2DBTableCepRules table = new H2DBTableCepRules();
		
		//SENSOR RESOURCES (CEP DATA)
    	String targetId = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME + "/" + deviceName + "/" + dataName;
    	Container cnt = new Container();
		cnt.setMaxNrOfInstances(BigInteger.valueOf(10));
		
    	// Create the CEP_DATA container
		ResponsePrimitive resp = RequestSender.deleteContainer(targetId);
		
		if(resp.getResponseStatusCode().equals(ResponseStatusCode.DELETED)){
			CepRule cepRule = table.get(deviceName, dataName);
			cep.removeRule(cepRule);
			table.remove(cepRule.id);
		}
		table.close();
		
		System.out.println("[CEP INFO]: Rule with device name: '" + deviceName + "' and data name: '" + dataName + "' has been deleted.");
	}
	
	public ArrayList<CepRule> getAllCepRules(){
		H2DBTableCepRules table = new H2DBTableCepRules();
		ArrayList<CepRule> list = table.getAll();
		table.close();
		
		return list;
	}
	
	public ArrayList<Device> getAllCepDevices(){
		H2DBTableDevices devices = new H2DBTableDevices();
		ArrayList<Device> devicesArray = devices.getAll();
		devices.close();
		
		return devicesArray;
	}
	
	public boolean isRunning(){
		return server.isRunning();
	}
	
	public static void setDebug(boolean mode){
		debug = mode;
	}
	
	public static boolean isDebug(){
		return debug;
	}
	
}
