#Mirah Maven Plugin

This plugin allows you to incorporate Mirah sources with your regular Java projects.  This is different than the official Mirah maven plugin, which does not support two-way dependencies between Java and Mirah.

##Usage

~~~
<build>
...
    <plugins>
    ...
      <plugin>
        <groupId>ca.weblite</groupId>
        <artifactId>maven-mirah-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
          <execution>
            <phase>process-sources</phase>
            <goals>
              <goal>compile</goal>
              <goal>testCompile</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      ...
    </plugins>
    ...
</build>
~~~