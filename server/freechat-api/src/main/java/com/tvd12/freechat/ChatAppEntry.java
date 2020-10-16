package com.tvd12.freechat;

import static com.tvd12.ezyfox.util.EzyAutoImplAnnotations.getBeanName;

import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.mongodb.MongoClient;
import com.tvd12.ezydata.hazelcast.factory.EzyMapTransactionFactory;
import com.tvd12.ezydata.morphia.EzyDataStoreBuilder;
import com.tvd12.ezydata.morphia.bean.EzyMorphiaRepositoriesImplementer;
import com.tvd12.ezyfox.bean.EzyBeanContextBuilder;
import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.context.EzyZoneContext;
import com.tvd12.ezyfoxserver.setting.EzyAppSetting;
import com.tvd12.ezyfoxserver.support.entry.EzySimpleAppEntry;
import com.tvd12.ezyfoxserver.support.factory.EzyAppResponseFactory;
import com.tvd12.freechat.config.ChatAppConfig;
import com.tvd12.properties.file.mapping.PropertiesMapper;
import com.tvd12.properties.file.reader.BaseFileReader;

import dev.morphia.Datastore;

public class ChatAppEntry extends EzySimpleAppEntry {

	@Override
	protected void preConfig(EzyAppContext ctx) {
		getLogger().info("\n=================== FREE CHAT APP START CONFIG ================\n");
	}
	
	@Override
	protected void postConfig(EzyAppContext ctx) {
		getLogger().info("\n=================== FREE CHAT APP END CONFIG ================\n");
	}
	
	@Override
	protected void setupBeanContext(EzyAppContext context, EzyBeanContextBuilder builder) {
		EzyAppSetting setting = context.getApp().getSetting();
		String appConfigFile = getConfigFile(setting);
		ChatAppConfig appConfig = readAppConfig(appConfigFile);
		String databaseName = appConfig.getDatabaseName(); 
		EzyZoneContext zoneContext = context.getParent();
		MongoClient mongoClient = zoneContext.get(MongoClient.class);
		HazelcastInstance hzInstance = zoneContext.get(HazelcastInstance.class);
		EzyMapTransactionFactory mapTransactionFactory = zoneContext.get(EzyMapTransactionFactory.class);
		Datastore datastore = newDatastore(mongoClient, databaseName);
		builder.addSingleton("mongoClient", mongoClient);
		builder.addSingleton("datastore", datastore);
		builder.addSingleton("hzInstance", hzInstance);
		builder.addSingleton("hazelcastInstance", hzInstance);
		builder.addSingleton("mapTransactionFactory", mapTransactionFactory);
		addAutoImplMongoRepo(builder, datastore);
	}
	
	protected String getConfigFile(EzyAppSetting setting) {
		return setting.getConfigFile();
	}
	
	public void start() throws Exception {
		getLogger().info("start free chat app");
	}

	@Override
	protected String[] getScanableBeanPackages() {
		return new String[] {
				"com.tvd12.freechat.repo",
				"com.tvd12.freechat.controller",
				"com.tvd12.freechat.handler",
				"com.tvd12.freechat.config",
				"com.tvd12.freechat.component",
				"com.tvd12.freechat.common.repo",
				"com.tvd12.freechat.common.service",
				"com.tvd12.freechat.service"
		};
	}
	
	@Override
	protected String[] getScanableBindingPackages() {
		return new String[] {
			"com.tvd12.freechat"
		};
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class[] getSingletonClasses() {
		return new Class[] {
			EzyAppResponseFactory.class
		};
	}
	
	private void addAutoImplMongoRepo(EzyBeanContextBuilder builder, Datastore datastore) {
		Map<Class<?>, Object> additionalRepo = implementMongoRepo(datastore);
		for (Class<?> repoType : additionalRepo.keySet()) {
			builder.addSingleton(getBeanName(repoType), additionalRepo.get(repoType));
		}
	}

	private Map<Class<?>, Object> implementMongoRepo(Datastore datastore) {
		return new EzyMorphiaRepositoriesImplementer()
				.scan("com.tvd12.freechat.repo")
				.implement(datastore);
	}

	private Datastore newDatastore(MongoClient mongoClient, String databaseName) {
		return EzyDataStoreBuilder.dataStoreBuilder()
				.mongoClient(mongoClient)
				.databaseName(databaseName)
				.scan("com.tvd12.freechat.data")
				.build();
	}
	
	private ChatAppConfig readAppConfig(String appConfigFile) {
		return new PropertiesMapper()
				.file(appConfigFile)
				.context(getClass())
				.clazz(ChatAppConfig.class)
				.reader(new BaseFileReader())
				.map();
	}

}
