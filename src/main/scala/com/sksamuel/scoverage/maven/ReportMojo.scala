package com.sksamuel.scoverage.maven

import org.apache.maven.plugins.annotations.{Component, Mojo, ResolutionScope}
import org.apache.maven.project.MavenProject
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.settings.Settings
import org.apache.maven.plugin.AbstractMojo
import java.io.File
import scoverage.{Env, IOUtils}
import scoverage.report.{ScoverageHtmlWriter, ScoverageXmlWriter, CoberturaXmlWriter}

/** @author Stephen Samuel */
@Mojo(name = "report",
  threadSafe = false,
  requiresDependencyResolution = ResolutionScope.TEST,
  defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.TEST)
class ReportMojo extends AbstractMojo {

  @Component
  var project: MavenProject = _

  @Component
  var plugin: PluginDescriptor = _

  @Component
  var settings: Settings = _

  def execute() {

    val coverage = IOUtils.deserialize(getClass.getClassLoader, Env.coverageFile(project.getBuild.getOutputDirectory))
    val measurements = IOUtils.invoked(Env.measurementFile(project.getBuild.getOutputDirectory))

    coverage.apply(measurements)

    val targetDirectory = new File(project.getBuild.getOutputDirectory + "/coverage-report")
    targetDirectory.mkdirs()

    getLog.info("[scoverage] Generating cobertura XML report...")
    new CoberturaXmlWriter(project.getBasedir, targetDirectory).write(coverage)

    getLog.info("[scoverage] Generating scoverage XML report...")
    new ScoverageXmlWriter(new File(project.getBuild.getSourceDirectory), targetDirectory).write(coverage)

    getLog.info("[scoverage] Generating scoverage HTML report...")
    new ScoverageHtmlWriter(new File(project.getBuild.getSourceDirectory), targetDirectory).write(coverage)
  }
}
