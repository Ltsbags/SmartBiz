import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import * as request from 'supertest';
import { AppModule } from './../src/app.module';

describe('SmartBiz Backend Engine (E2E Integration)', () => {
  let app: INestApplication;

  beforeAll(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleFixture.createNestApplication();
    app.setGlobalPrefix('api');
    app.useGlobalPipes(new ValidationPipe({ transform: true }));
    await app.init();
  });

  afterAll(async () => {
    await app.close();
  });

  it('/api/health (GET) - Liveness check should return healthy', () => {
    return request(app.getHttpServer())
      .get('/api/health')
      .expect(200)
      .expect((res) => {
        expect(res.body.status).toBeDefined();
        expect(res.body.services).toBeDefined();
      });
  });

  it('/api/v1/auth/register (POST) - Validation check', () => {
    return request(app.getHttpServer())
      .post('/api/v1/auth/register')
      .send({ email: 'invalid-email' })
      .expect(400);
  });
});
