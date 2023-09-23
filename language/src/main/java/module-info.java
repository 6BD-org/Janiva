
module com.xmbsmdsj.janiva.lang {

    requires org.graalvm.truffle;
    requires org.slf4j;
    requires lombok;
    requires truffle.dsl.processor;

    exports com.xmbsmdsj.janiva.io;



     uses com.oracle.truffle.jx.JanivaLangProvider;
     provides com.oracle.truffle.api.provider.TruffleLanguageProvider with com.oracle.truffle.jx.JanivaLangProvider;
}